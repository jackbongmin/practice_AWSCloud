#!/usr/bin/env bash
# 11_01: EC2(Ubuntu 24.04)에서 처음 한 번 — Docker와 Compose 설치 (git clone 뒤 bash setup-docker.sh)
set -euo pipefail
# Docker 공식 설치 스크립트 (Docker Engine + Compose 플러그인). 연습·수업용으로 공식 문서가 안내하는 방법
curl -fsSL https://get.docker.com | sudo sh
sudo systemctl enable --now docker        # 부팅할 때 Docker 자동 시작
sudo usermod -aG docker "$(whoami)"           # sudo 없이 docker 명령 — 다시 접속해야 적용된다
docker --version; docker compose version
