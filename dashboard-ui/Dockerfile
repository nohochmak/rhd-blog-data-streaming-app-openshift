FROM node:18.20.2-alpine3.19 AS build
WORKDIR /usr/src/app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:1.25.4-alpine3.18
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /usr/src/app/dist/dashboard-ui /usr/share/nginx/html
RUN chgrp -R 0 /var/cache/nginx && chmod -R g+rwX /var/cache/nginx/
RUN chgrp -R 0 /var/run && chmod -R g+rwX /var/run
RUN chgrp -R 0 /usr/share/nginx/html && chmod -R g+rwX /usr/share/nginx/html
EXPOSE 8080
CMD ["/bin/sh",  "-c",  "envsubst < /usr/share/nginx/html/assets/env.template.js > /usr/share/nginx/html/assets/env.js && exec nginx -g 'daemon off;'"]
