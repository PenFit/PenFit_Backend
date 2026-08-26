# 배포 가이드

OCI Ampere A1 (ARM64) + Docker + GitHub Actions 구성이다.

```
develop 푸시 / PR   → ① 테스트까지만
main 푸시           → ① 테스트 → ② 이미지 빌드 → ③ 배포
```

| 단계 | 실행 위치 | 내용 |
|---|---|---|
| ① 테스트 | ubuntu-latest | postgres:16 서비스 컨테이너와 함께 `./gradlew test` |
| ② 이미지 빌드 | ubuntu-latest | `linux/amd64` 로 빌드해 GHCR 에 커밋 SHA 태그로 푸시 |
| ③ 배포 | SSH | compose·Caddyfile 전송 → 시크릿 주입 → pull & up -d → 헬스체크 |

## 값 주입 방식

**서버 디스크에 비밀값을 파일로 남기지 않는다.** `.env` 파일을 만들지 않고,
배포 SSH 세션의 환경변수로만 값을 전달한다. `docker compose` 가 `${VAR}` 를 셸 환경에서
치환하므로 이것만으로 충분하다. 세션이 끝나면 서버에는 아무 값도 남지 않는다.

배포 스크립트는 시작 시 필수 값이 모두 채워졌는지 먼저 검사하고, 하나라도 비면
컨테이너를 올리지 않고 중단한다. 값이 빈 채로 조용히 잘못 뜨는 상황을 막기 위해서다.
과거 배포가 남긴 `.env` 가 있으면 함께 삭제한다.

## 리소스 예산

배포 대상은 Always Free AMD 인스턴스(1 OCPU / 1GB)다.
Ampere A1은 이 계정의 `standard-a1-core-count` 서비스 제한이 0이라 생성할 수 없다.

메모리 제한을 걸고 실측한 값이다. (pgbench 동시 10 + HTTP 동시 200 부하 기준)

| 구성요소 | 실측 피크 | 컨테이너 제한 |
|---|---|---|
| Spring Boot | 259MB | 420MB |
| PostgreSQL | 143MB | 192MB |
| Caddy | 27MB | 48MB |
| 합계 | 429MB | 660MB |

OS와 Docker 데몬이 약 220MB를 쓰므로 실사용은 약 650MB / 1,024MB다.

적용한 튜닝:

- JVM `-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:MaxDirectMemorySize=32m -XX:+UseSerialGC -Xss512k`
  1 OCPU 환경에서 G1GC는 오버헤드가 커서 SerialGC를 쓴다.
- Tomcat 최대 스레드 200 → 50. 스레드당 스택 512KB라 기본값이면 최대 100MB를 쓴다.
- HikariCP 커넥션 풀 10. PostgreSQL `max_connections=20`보다 작아야 한다.
- PostgreSQL `shared_buffers=64MB`, 병렬 워커 축소

AI 서버(FastAPI)는 이 서버에 올리지 않는다. 남는 여유가 약 370MB인데
FastAPI가 150~250MB를 쓰면 안전 마진이 사라진다. 별도 호스팅 후 `AI_BASE_URL`만 연결한다.

## 서버 사전 준비

1. **인스턴스** — `VM.Standard.E2.1.Micro` (1 OCPU / 1GB, x86_64) + `Canonical Ubuntu 22.04`
   Ampere A1은 이 계정의 `standard-a1-core-count` 한도가 0이라 생성할 수 없다.
   접속 후 `uname -m` 이 `x86_64` 인지 확인한다.

2. **포트 개방 — 두 군데 모두 해야 한다**
   - OCI 콘솔: Subnet → Security List → Ingress 에 `0.0.0.0/0` TCP 80, 443
   - 서버 안 iptables (Ubuntu 이미지는 기본 차단이다)
     ```bash
     sudo apt update && sudo apt install -y iptables-persistent
     sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
     sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
     sudo netfilter-persistent save
     ```

3. **스왑 4GB** — 메모리가 1GB뿐이라 안전망이 필요하다
   ```bash
   sudo fallocate -l 4G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
   echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
   ```

4. **Docker 설치**
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER
   ```
   실행 후 SSH를 끊고 다시 접속해야 그룹 권한이 적용된다.

5. **도메인** — DuckDNS 에서 서브도메인을 만들고 `current ip` 에 인스턴스 공인 IP를 등록한다.
   Caddy가 Let's Encrypt 인증서를 자동 발급한다.
   프론트엔드는 Vercel rewrites 로 `/api/*` 를 이 주소로 프록시한다. 브라우저 기준 동일 출처가 되어
   Refresh Token 쿠키가 퍼스트파티로 동작하고 CORS 설정도 필요 없다.

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

롤백은 **Actions 에서 이전 커밋의 워크플로를 다시 실행**하는 방식을 쓴다.
서버에 값이 남아 있지 않으므로 서버에서 직접 `docker compose up` 을 실행하면 환경변수가 없어 실패한다.

```bash
# 서버에서는 상태 확인과 로그 조회만 한다
docker compose -f docker-compose.prod.yml ps
docker logs -f penfit-app
```

## 보안 구성

| 항목 | 조치 |
|---|---|
| 포트 | 80·443 만 개방. DB(5432)와 앱(8080)은 컨테이너 네트워크 안에만 존재 |
| 비밀값 | 서버 디스크에 저장하지 않음. 배포 SSH 세션 환경변수로만 전달 |
| 레지스트리 자격증명 | pull 직후 `docker logout` 으로 제거 |
| 컨테이너 실행 계정 | 루트가 아닌 `penfit` 사용자 |
| `/actuator` | Caddy 가 외부 요청을 404 로 차단. 노출 엔드포인트도 `health` 하나로 제한하고 상세 정보는 숨김 |
| 응답 헤더 | HSTS, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy` 적용. `Server` 헤더 제거 |
| 요청 본문 | 2MB 상한 (파일 업로드 기능이 없음) |
| Swagger UI | 심사 편의를 위해 공개 유지 |

## 참고

- AI 서버 컨테이너는 준비되면 `docker-compose.prod.yml` 에 `ai-server` 서비스로 추가한다.
  이때도 포트를 외부에 열지 않고 컨테이너 네트워크 안에서만 접근한다.
