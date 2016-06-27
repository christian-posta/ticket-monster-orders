#!/usr/bin/env bash
kubectl scale rc debezium-mysql-orders --replicas=0
kubectl scale rc ticket-monster-admin-camel --replicas=0
kubectl scale rc kafka --replicas=0
kubectl scale rc zk-standalone --replicas=0
