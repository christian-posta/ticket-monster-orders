#!/usr/bin/env bash
kubectl scale rc zk-standalone --replicas=1
kubectl scale rc kafka --replicas=1
kubectl scale rc debezium-mysql-orders --replicas=1
#kubectl scale rc ticket-monster-admin-camel --replicas=1
