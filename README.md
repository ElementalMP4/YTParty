# YouTubeParty

[![Project Status: Active](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active) [![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Build Status](https://jenkins.voidtech.de/buildStatus/icon?job=YTParty)](https://jenkins.voidtech.de/job/YTParty/)

Watch YouTube with friends!

## Running YTParty

YTParty requires a config file as such:

```conf
#Hibernate Options
hibernate.User=YTParty
hibernate.Password=password
hibernate.ConnectionURL=jdbc:postgresql://localhost:5432/YTParty
#Tomcat options
http.port=8080
#Cache options
cache.TextIsEnabled=true
cache.BinaryIsEnabled=true
#HCaptcha Token
hcaptcha.Token=a_nice_token
```

As well as a PostgreSQL server. PGAdmin is recommended for database management.
