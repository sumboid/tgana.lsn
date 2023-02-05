FROM clojure:temurin-19-alpine AS bootstrap

RUN apk add nodejs npm

WORKDIR /build
COPY . .
RUN npm ci
RUN npm run pkg:alpine

# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

FROM alpine

# RUN apk add curl
WORKDIR /
COPY --from=bootstrap /build/dist/tglsnr-alpine /tglsnr

# HEALTHCHECK CMD curl --fail http://localhost:3000/health 2> /dev/null || exit 1 

ENTRYPOINT ["/tglsnr"]
