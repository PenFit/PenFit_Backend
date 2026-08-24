# 배포 가이드

OCI Ampere A1 (ARM64) + Docker + GitHub Actions 구성이다.

```
main 푸시
  ↓
① 테스트        ubuntu-latest + postgres:16
② 이미지 빌드    ubuntu-24.04-arm 에서 네이티브 ARM64 빌드 → GHCR 푸시
③ 배포          compose·Caddyfile 전송 → 시크릿으로 .env 생성 → pull & up -d → 헬스체크
```

`.env` 는 **배포할 때마다 GitHub Secrets 값으로 새로 생성**된다. 서버에서 직접 만들거나 수정하지 않는다.

## 서버 사전 준비

1. **인스턴스** — Ubuntu 22.04 / `VM.Standard.A1.Flex` / 2 OCPU / 12GB
   Always Free 한도는 4 OCPU · 24GB다. AMD Micro(1GB)로는 동작하지 않는다.

2. **포트 개방 — 두 군데 모두 해야 한다**
   - OCI 콘솔: Subnet → Security List → Ingress 에 `0.0.0.0/0` TCP 80, 443
   - 서버 안 iptables (Ubuntu 이미지는 기본 차단이다)
     ```bash
     sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
     sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
     sudo netfilter-persistent save
     ```

3. **Docker 설치**
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER
   ```
   실행 후 SSH를 끊고 다시 접속해야 그룹 권한이 적용된다.

4. **도메인** — 공인 IP를 가리키도록 A 레코드를 설정한다. Caddy가 Let's Encrypt 인증서를 자동 발급한다.

## GitHub 설정

저장소 → Settings → Secrets and variables → Actions

### Secrets
| 이름 | 설명 |
|---|---|
| `OCI_HOST` | 인스턴스 공인 IP |
| `OCI_USER` | SSH 사용자 (Ubuntu 이미지는 `ubuntu`) |
| `OCI_SSH_KEY` | SSH 개인키 전문 (`-----BEGIN` ~ `-----END`) |
| `POSTGRES_DB` | 데이터베이스 이름 |
| `POSTGRES_USER` | 데이터베이스 사용자 |
| `POSTGRES_PASSWORD` | 데이터베이스 비밀번호 |
| `AI_INTERNAL_API_KEY` | AI 서버 내부 API 키 |

### Variables
| 이름 | 값 | 설명 |
|---|---|---|
| `DEPLOY_ENABLED` | `true` | 배포 잡 활성화. 없으면 배포 단계를 건너뛴다 |
| `PENFIT_DOMAIN` | 예) `penfit.duckdns.org` | Caddy가 인증서를 발급할 도메인 |
| `AI_BASE_URL` | `http://ai-server:8000` | AI 서버 주소 |

## 수동 배포와 롤백

```bash
# 특정 커밋 버전으로 되돌리기
cd ~/penfit
sed -i "s|^PENFIT_IMAGE=.*|PENFIT_IMAGE=ghcr.io/penfit/penfit_backend:<커밋SHA>|" .env
docker compose -f docker-compose.prod.yml up -d

# 로그 확인
docker logs -f penfit-app
docker compose -f docker-compose.prod.yml ps
```

## 참고

- DB와 애플리케이션 포트는 외부에 열지 않는다. 외부 트래픽은 전부 Caddy(80/443)를 거친다.
- AI 서버 컨테이너는 준비되면 `docker-compose.prod.yml` 에 `ai-server` 서비스로 추가한다.
