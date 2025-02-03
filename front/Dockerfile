# Stage 1: Build Environment
FROM builder-image AS build-stage
# Install build tools (e.g., Maven, Gradle)
# Copy source code
# Build commands (e.g., compile, package)
FROM node:lts-alpine
COPY package*.json ./
RUN npm install
RUN npm run build

# Stage 2: Runtime environment
FROM runtime-image AS final-stage
#  Copy application artifacts from the build stage (e.g., JAR file)
COPY dist/front/browser/ /usr/share/nginx/html
COPY conf-nginx/nginx.conf /etc/nginx/conf.d/default.conf
