# Introduction to Using AtlasX-Gateway

AtlasX-Gateway is a powerful tool designed to serve as a gateway within the Gaia-X ecosystem.
This guide will walk you through the essential steps to get started with AtlasX-Gateway, 
including creating your own self-description (SD), setting up the docker-compose file, configuring your domain, and creating a service offering.

## Step 1: Preparation

In this example, we will create a simple Docker Compose setup that contains all the necessary files.
Create the following folder structure:

```shell
ATLAS-X-GATEWAY/
└─ vc_templates/
└─ hkv_example/
└─ docker-compose.yml
```

## Step 2: Creating Your Own Self-Description (SD)

A Self-Description (SD) is a JSON file that describes the credentials and attributes of your service. To create your own SD, follow these steps:
1. Create a new JSON file named `GaiaxCredentialSD.json` in the vc_templates folder.
2. Populate the file with the necessary self-description attributes. Refer to the official documentation for the required schema.

Example:
```json
{
  "@context": ["https://www.w3.org/2018/credentials/v1"],
  "id": null,
  "issued": null,
  "issuer": null,
  "type": ["VerifiableCredential", "SelfDescription"],
  "credentialSubject": {
    "brandName": "Example Brand",
    "id": null,
    "commercialRegister": {
      "countryName": "Germany"
    },
    "role":[],
    "corporateEmailAddress": "contact@emergency.dc",
    "individualContactLegal": "legal@emergency.dc",
    "individualContactTechnical": "support@emergency.dc",
    "jurisdiction": "Germany",
    "legalForm": "GmbH",
    "legalRegistrationNumber": "HRB 0815",
    "legallyBindingAddress": {
      "countryName": "Germany"
    },
    "legallyBindingName": "Example Brand GmbH",
    "webAddress": "https://example.com"
  }
}
```

## Step 3: Creating a Docker-Compose File

To run AtlasX-Gateway using Docker, you'll need a `docker-compose.yml` file. Below is a sample docker-compose file:

```yaml
services:
  atlasgw:
    image: ghcr.io/chainstep/atlasx-gateway:latest
    volumes:
      - /app/gx/atlas/vc-templates:/app/resources/vc-templates
      - /app/gx/atlas/hkv-example:/hkv-example
    environment:
      ATLAS_CONFIG_DOMAIN: my-gateway.example.com
      ATLAS_CONFIG_SERVICE_BASE_ADDRESS: https://my-gateway.example.com
      ATLAS_CONFIG_CATALOGUE_REFRESH_RATE: 60
      ATLAS_CONFIG_SELF_DESCRIPTION_REFRESH_RATE: 60
      SERVICEOFFERINGS_SERVICES_0_NAME: ServiceOfferingHealth
      SERVICEOFFERINGS_SERVICES_0_URL: http://localhost:8080/actuator/health
```

## Step 4: Setting the Domain in the Configuration

Setting the domain is crucial for the deployment and accessibility of your gateway. Ensure that the domain is publicly available on the internet.

In the `docker-compose.yml` file, set the environment variables `ATLAS_CONFIG_DOMAIN` and `ATLAS_CONFIG_SERVICE_BASE_ADDRESS` to your desired domain. For example:

```yaml
environment:
  ATLAS_CONFIG_DOMAIN: my-gateway.example.com
  ATLAS_CONFIG_SERVICE_BASE_ADDRESS: https://my-gateway.example.com
```

This will ensure that the gateway is deployed to `my-gateway.example.com` and is accessible over the internet.

## Step 5: Creating a Service Offering

Service offerings define the services provided by your gateway. You can specify them in the `docker-compose.yml` file under the `environment` section.

Example:

```yaml
environment:
  SERVICEOFFERINGS_SERVICES_0_NAME: HealthCheckService
  SERVICEOFFERINGS_SERVICES_0_URL: http://localhost:8080/actuator/health
```

This configuration creates the offering: `HealthCheckService` which simply exposes the health endpoint of the gateway 
itself for testing purposes.

## Step 6: Startup

To start your setup use

```shell
docker compose up -d
```

If you start the Gateway for the first time (or you have an empty hkv-example folder), the Gateway will use the
Self-Description and your configured domain to register itself at the **Cartrust authority**.

It will regularly check if the registration has been accepted by the authority. As long as the registration is not 
accepted, you will see an info message similar to this in the logs:

```shell
Exception while communicating: 425 : "{"timestamp":1726579731678,"status":425,"error":"Too Early","path":"/trust"}"
```

In the testing environment, there is a URL that you can use to register yourself: http://<super_secret_url>
After being registered, you can continue with the next step.

## Step 7: Public endpoint configuration

The AtlasX-Gateway will make the configured services available to the public under the `/proxy` path. Additionally, the
path `/gx` will publish some static documents needed for SSI to work.

Make sure that the paths `/gx` and `/proxy` are available to the outside by configuring any proxy or firewall that may
reside between your AtlasX-Gateway and the Internet.

## Step 8: DID check

After being successfully registered at the authority, you should see your VC in the logs. It starts with `web:did` and
looks similar to this one:

``did:web:my-gateway.example.com:gx:did:286e58fc4daf495c96d6be8302d5cdc7``

Copy the DID and go to [Universal Resolver](https://dev.uniresolver.io/). Enter your DID in the **did-url** field and
select **Resolve**. You should get some information about your DID.
In the background, the Universal Resolver resolved your did:web to a URL, connected to your Gateway and downloaded the 
DID document file from it. The DID document file is created and publicly offered by the Gateway after successful
registration at the Cartrust authority. When your gateway connects to another AtlasX-Gateway, then this other Gateway 
will resolve your DID document in the same way as the Universal Resolver and use your DID document to validate your VC.

This is the reason why a AtlasX-Gateway must be publicly available.

## Step 9: Consuming Data

Use the REST API of your gateway to consume data from another gateway. E.g. to consume the ServiceOfferingExample 
resource from another gateway, you can send the following request to your gateway:

```shell
curl --location --request GET 'http://your_gateway_internal_url:8080/internal-proxy' \
--header 'X-Service-Url: https://other_gateway/proxy/ServiceOfferingExample' \
--header 'x-api-key: 1234567890'
```

The placeholder `your_gateway_internal_url` should be replaced with the internal URL of your gateway. 

Put the URL of the service offering of the other gateway into the `X-Service-Url` header field.

The `x-api-key` is what you have configured for you gateway in the `docker-compose.yml` above.

In the same way another gateway can consume the ServiceOfferingHealth (see `docker-compose.yml' file above) offering
from your gateway with the following request:

```shell
curl --location --request GET 'http://other_gateway_internal_url:8080/internal-proxy' \
--header 'X-Service-Url: https://my-gateway.example.com/proxy/ServiceOfferingHealth' \
--header 'x-api-key: other_api_key'
```

### Conclusion

By following these steps, you can successfully set up and run AtlasX-Gateway using Docker. This guide covered creating a self-description, setting up the docker-compose file, configuring the domain, and defining service offerings. For more detailed information, refer to the official AtlasX-Gateway documentation.
