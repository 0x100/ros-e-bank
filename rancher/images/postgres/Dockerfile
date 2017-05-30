FROM postgres:9.6.3-alpine
ADD create-users.sh /docker-entrypoint-initdb.d/create-users.sh
RUN chmod +x /docker-entrypoint-initdb.d/create-users.sh
ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 5432
CMD ["postgres"]