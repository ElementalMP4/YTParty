# YouTubeParty

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
```

As well as a PostgreSQL server. PGAdmin is recommended for database management.