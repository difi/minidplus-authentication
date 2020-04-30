#!/usr/bin/env bash
. /usr/local/webapps/application.conf
LOG_ENV=$(cut -d'=' -f2 /log_config)||:
/usr/share/filebeat/bin/filebeat -c /etc/filebeat/filebeat.yml -path.home /usr/share/filebeat -path.config /etc/filebeat -path.data /var/lib/filebeat -path.logs /var/log/filebeat -E LOG_ENV=$LOG_ENV &
java -jar /usr/local/webapps/application.jar $RUN_ARGS
