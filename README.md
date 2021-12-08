# YouTubeParty

[![Project Status: Active](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active) [![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Build Status](https://jenkins.voidtech.de/buildStatus/icon?job=YTParty)](https://jenkins.voidtech.de/job/YTParty/)

Watch YouTube with friends!

[Check us out here!](https://ytparty.voidtech.de/)

## Running YTParty

YTParty requires a config file named `config.properties` with the following fields:

```conf
#Hibernate Options
hibernate.User=YTParty
hibernate.Password=n0th1n9_t0_533_h3r3
hibernate.ConnectionURL=jdbc:postgresql://localhost:5432/YTParty
#Tomcat options
http.port=8080
#Cache options
cache.TextIsEnabled=true
cache.BinaryIsEnabled=true
#reCaptcha Token
captcha.Token=n0_h1dd3n_m355ag35
#Mail Options
mail.User=noreply
mail.Address=noreply@ytparty.voidtech.de
mail.Password=v3ry_5tr0ng_P4SSw0rd
mail.Host=smtp.gmail.com
mail.Port=465
#Particles options
particles.Mode=regular
```

As well as a PostgreSQL server. Ensure that the database used in the connection URL exists on the PostgreSQL server. PGAdmin is recommended for database management.
