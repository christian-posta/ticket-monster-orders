Install ZK
Install Kafka
Install connect

Port forward locally 8083


Create connector:
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @admin-connector.json

Delete connecto:

curl -i -X DELETE -H "Accept:application/json" http://localhost:8083/connectors/admin-connector
