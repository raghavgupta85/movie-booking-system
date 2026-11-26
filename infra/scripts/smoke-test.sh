#!/usr/bin/env bash
# Week 1 smoke test. Usage: infra/scripts/smoke-test.sh   (backend must be running)
BASE="${BASE:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@moviebooking.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin@12345}"
JSON='Content-Type: application/json'
PASS=0; FAIL=0

check()   { if [ "$2" = "$3" ]; then echo "PASS  $1"; PASS=$((PASS+1)); else echo "FAIL  $1 (expected $2, got $3)"; FAIL=$((FAIL+1)); fi; }
code()    { curl -s -o /dev/null -w "%{http_code}" "$@"; }
firstid() { grep -o '"id":[0-9]*' | head -1 | cut -d: -f2; }
token()   { curl -s -X POST "$BASE/api/auth/login" -H "$JSON" -d "{\"email\":\"$1\",\"password\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }

RUN=$(date +%s)
DAY=$(date -d "+30 days" +%F)
EMAIL="smoke$RUN@test.com"

echo "== health and auth =="
check "health" 200 "$(code "$BASE/api/health")"
check "register" 201 "$(code -X POST "$BASE/api/auth/register" -H "$JSON" -d "{\"name\":\"Smoke\",\"email\":\"$EMAIL\",\"password\":\"password123\"}")"
check "register duplicate" 409 "$(code -X POST "$BASE/api/auth/register" -H "$JSON" -d "{\"name\":\"Smoke\",\"email\":\"$EMAIL\",\"password\":\"password123\"}")"
check "login wrong password" 401 "$(code -X POST "$BASE/api/auth/login" -H "$JSON" -d "{\"email\":\"$EMAIL\",\"password\":\"nope-nope\"}")"
USER_TOKEN=$(token "$EMAIL" password123)
ADMIN_TOKEN=$(token "$ADMIN_EMAIL" "$ADMIN_PASSWORD")
check "user token issued" eyJ "${USER_TOKEN:0:3}"
check "admin token issued" eyJ "${ADMIN_TOKEN:0:3}"
U="Authorization: Bearer $USER_TOKEN"
A="Authorization: Bearer $ADMIN_TOKEN"

echo "== catalog =="
check "user cannot create movie" 403 "$(code -X POST "$BASE/api/movies" -H "$U" -H "$JSON" -d '{"title":"x","durationMinutes":100}')"
check "no token cannot create movie" 401 "$(code -X POST "$BASE/api/movies" -H "$JSON" -d '{"title":"x","durationMinutes":100}')"
MOVIE=$(curl -s -X POST "$BASE/api/movies" -H "$A" -H "$JSON" \
  -d "{\"title\":\"Smoke Movie $RUN\",\"genre\":\"Test\",\"language\":\"English\",\"durationMinutes\":120}" | firstid)
check "admin creates movie" yes "$([ -n "$MOVIE" ] && echo yes || echo no)"
check "get movie" 200 "$(code "$BASE/api/movies/$MOVIE")"
THEATRE=$(curl -s -X POST "$BASE/api/theatres" -H "$A" -H "$JSON" -d "{\"name\":\"Smoke Theatre $RUN\",\"city\":\"Smokeville\"}" | firstid)
check "admin creates theatre" yes "$([ -n "$THEATRE" ] && echo yes || echo no)"
SCREEN=$(curl -s -X POST "$BASE/api/theatres/$THEATRE/screens" -H "$A" -H "$JSON" -d '{"name":"Screen 1"}' | firstid)
check "admin creates screen" yes "$([ -n "$SCREEN" ] && echo yes || echo no)"
check "generate seats" 201 "$(code -X POST "$BASE/api/screens/$SCREEN/seats/generate" -H "$A" -H "$JSON" -d '{"rows":3,"seatsPerRow":4}')"
check "generate seats twice" 409 "$(code -X POST "$BASE/api/screens/$SCREEN/seats/generate" -H "$A" -H "$JSON" -d '{"rows":3,"seatsPerRow":4}')"

echo "== shows =="
SHOW=$(curl -s -X POST "$BASE/api/shows" -H "$A" -H "$JSON" \
  -d "{\"movieId\":$MOVIE,\"screenId\":$SCREEN,\"startTime\":\"${DAY}T19:00:00\",\"price\":250}" | firstid)
check "create show" yes "$([ -n "$SHOW" ] && echo yes || echo no)"
check "overlapping show rejected" 409 "$(code -X POST "$BASE/api/shows" -H "$A" -H "$JSON" -d "{\"movieId\":$MOVIE,\"screenId\":$SCREEN,\"startTime\":\"${DAY}T20:00:00\",\"price\":250}")"
check "back-to-back show allowed" 201 "$(code -X POST "$BASE/api/shows" -H "$A" -H "$JSON" -d "{\"movieId\":$MOVIE,\"screenId\":$SCREEN,\"startTime\":\"${DAY}T22:00:00\",\"price\":250}")"
check "user cannot create show" 403 "$(code -X POST "$BASE/api/shows" -H "$U" -H "$JSON" -d "{\"movieId\":$MOVIE,\"screenId\":$SCREEN,\"startTime\":\"${DAY}T23:30:00\",\"price\":250}")"
check "search by movie" '"totalElements":2' "$(curl -s "$BASE/api/shows?movieId=$MOVIE" | grep -o '"totalElements":[0-9]*')"
check "search by city" '"totalElements":2' "$(curl -s "$BASE/api/shows?city=smokeville&movieId=$MOVIE" | grep -o '"totalElements":[0-9]*')"
check "seat map: 12 available" '"availableSeats":12' "$(curl -s "$BASE/api/shows/$SHOW/seats" | grep -o '"availableSeats":[0-9]*')"
check "cannot delete movie with shows" 409 "$(code -X DELETE "$BASE/api/movies/$MOVIE" -H "$A")"

echo
echo "Passed: $PASS   Failed: $FAIL"
[ "$FAIL" -eq 0 ]
