import json
import os
import sys
import time
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

FIXTURES = Path(__file__).resolve().parent.parent / "src/test/resources/fixtures/penfit-ai-success-responses.json"

ROUTES = {
    "/internal/v1/pension-passport/analyze": "pensionPassportAnalyze",
    "/internal/v1/pension-plan/generate": "pensionPlanGenerate",
    "/internal/v1/product-recommendations/generate": "productRecommendationsGenerate",
    "/internal/v1/spending-mission/analyze": "spendingMissionAnalyze",
}

DELAY_SECONDS = float(os.environ.get("MOCK_DELAY_SECONDS", "0"))
FAIL_STATUS = int(os.environ.get("MOCK_FAIL_STATUS", "0"))
FAIL_CODE = os.environ.get("MOCK_FAIL_CODE", "ANALYSIS_FAILED")


def load_fixtures():
    with FIXTURES.open(encoding="utf-8") as file:
        return json.load(file)


class Handler(BaseHTTPRequestHandler):

    protocol_version = "HTTP/1.1"

    def do_GET(self):
        if self.path == "/internal/health/live":
            self.respond(200, {"status": "UP"})
        else:
            self.respond(404, {"code": "NOT_FOUND", "message": self.path})

    def do_POST(self):
        body = self.read_body()

        if not self.headers.get("X-Internal-Api-Key"):
            self.respond(401, {"code": "INTERNAL_API_KEY_INVALID", "message": "missing api key"})
            return

        key = ROUTES.get(self.path)
        if key is None:
            self.respond(404, {"code": "NOT_FOUND", "message": self.path})
            return

        print(f"[mock-ai] {self.path}\n{body}\n", flush=True)

        if DELAY_SECONDS > 0:
            time.sleep(DELAY_SECONDS)

        if FAIL_STATUS:
            self.respond(FAIL_STATUS, {"code": FAIL_CODE, "message": "mock failure"})
            return

        fixtures = load_fixtures()
        if key not in fixtures:
            self.respond(501, {"code": "MODEL_ERROR", "message": f"fixture 없음: {key}"})
            return

        response = fixtures[key]
        if key == "pensionPassportAnalyze":
            response = reflect_answers(response, body)
        if key == "productRecommendationsGenerate":
            response = reflect_candidates(response, body)

        self.respond(200, response)

    def read_body(self):
        if "chunked" in self.headers.get("Transfer-Encoding", "").lower():
            chunks = []
            while True:
                size = int(self.rfile.readline().split(b";")[0].strip() or b"0", 16)
                if size == 0:
                    self.rfile.readline()
                    break
                chunks.append(self.rfile.read(size))
                self.rfile.readline()
            return b"".join(chunks).decode("utf-8")

        length = int(self.headers.get("Content-Length", 0))
        return self.rfile.read(length).decode("utf-8") if length else ""

    def respond(self, status, payload):
        encoded = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def log_message(self, fmt, *args):
        pass


def reflect_candidates(response, body):
    try:
        candidates = json.loads(body).get("productCandidates") or []
    except json.JSONDecodeError:
        return response
    if len(candidates) < len(response["recommendations"]):
        return response

    reflected = dict(response)
    reflected["recommendations"] = [
        {**item, "productId": candidates[index]["productId"]}
        for index, item in enumerate(response["recommendations"])
    ]
    return reflected


def reflect_answers(response, body):
    try:
        answers = json.loads(body).get("rehearsalAnswers") or []
    except json.JSONDecodeError:
        return response
    if not answers:
        return response

    templates = {item["scenarioCode"]: item for item in response["detailedAnalysis"]}
    reflected = dict(response)
    reflected["detailedAnalysis"] = [
        {**templates[answer["scenarioCode"]], "selectedOptionCode": answer["optionCode"]}
        for answer in answers
        if answer["scenarioCode"] in templates
    ]
    return reflected


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8000
    print(f"[mock-ai] http://localhost:{port} 에서 대기", flush=True)
    print(f"[mock-ai] 지연 {DELAY_SECONDS}초, 실패 상태 {FAIL_STATUS or '없음'}", flush=True)
    HTTPServer(("127.0.0.1", port), Handler).serve_forever()
