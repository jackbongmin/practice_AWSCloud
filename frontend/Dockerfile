# 11_02: 화면도 서버에서 빌드한다 — Node로 dist를 만들고, nginx 이미지에는 dist만 담는다 (멀티 스테이지)
FROM node:22-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.29-alpine
COPY --from=build /src/dist /usr/share/nginx/html
