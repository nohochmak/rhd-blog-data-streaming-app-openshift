## -----
2024 Oct 25 - This should be renamed as a branch of the repo for demo.redhat.com
Only need to change the following for running in OpenShift Local (CRC)
    emoji-tracker.yaml: 
      # For OpenShift Local (CRC)
      storageClassName: "crc-csi-hostpath-provisioner"

    kustomization.yaml  
        - MY_NAMESPACE=banderle1-dev
        - MY_SANDBOX_ID=.apps.sandbox-m3.1530.p1.openshiftapps.com
        - access-token=sha256~HS3K9RScdw24wjIX_UKH3VpBQwVqszJg_twWpbqqe0c

## Stateful and reactive stream processing applications with Apache Kafka, Quarkus and Angular on OpenShift

### What are you learning?

In this in-depth article, you're going to learn how to build an end-to-end reactive stream processing application using Apache Kafka as an event streaming platform, Quarkus for your backend, and a frontend written in Angular. In the end, you'll deploy all three containerized applications on the OpenShift Developer Sandbox.

**The final result is a colorful live dashboard tracking the usage of emojis in public 🐘 Mastodon 🐘 posts (a.k.a toots ).**

👉 Read the full article on the Red Hat Developer blog [here](https://developers.redhat.com/articles/2024/06/14/stateful-and-reactive-stream-processing-applications-apache-kafka-quarkus-and). 👈
