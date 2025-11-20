
# Schema Version 2

## Graveyard Table
```(sql)
  CREATE TABLE IF NOT EXISTS Graveyard (\
    Key INTEGER PRIMARY KEY AUTOINCREMENT, \
    SearchKey VARCHAR UNIQUE NOT NULL, \
    Name VARCHAR NOT NULL, \
    UidMsb BIGINT NOT NULL, \
    UidLsb BIGINT NOT NULL, \
    Enabled BOOLEAN NOT NULL ON CONFLICT REPLACE DEFAULT 1, \
    Hidden BOOLEAN NOT NULL ON CONFLICT REPLACE DEFAULT 1, \
    DiscoveryRange INTEGER, \
    DiscoveryMessage VARCHAR, \
    RespawnMessage VARCHAR, \
    SafetyRange INTEGER, \
    SafetyTime BIGINT, \
    GroupName VARCHAR, \
    WorldName VARCHAR NOT NULL, \
    WorldUidMsb BIGINT NOT NULL, \
    WorldUidLsb BIGINT NOT NULL, \
    X DOUBLE, \
    Y DOUBLE, \
    Z DOUBLE, \
    Yaw FLOAT, \
    Pitch FLOAT, \
    Created DATETIME, \
    CreatorUidMsb BIGINT, \
    CreatorUidLsb BIGINT, \
    UNIQUE(UidMsb, UidLsb))
```

| pk | name             | type     | unique | nullable | on conflict       |
|----|------------------|----------|--------|----------|-------------------|
| ✓  | Key              | INTEGER  |        |          |                   |
|    | SearchKey        | VARCHAR  | ✓      |          |                   |
|    | Name             | VARCHAR  |        |          |                   |
|    | UidMsb           | BIGINT   |        |          |                   |
|    | UidLsb           | BIGINT   |        |          |                   |
|    | Enabled          | BOOLEAN  |        |          | REPLACE DEFAULT 1 |
|    | Hidden           | BOOLEAN  |        |          | REPLACE DEFAULT 1 |
|    | DiscoveryRange   | INTEGER  |        |          |                   |
|    | DiscoveryMessage | VARCHAR  |        |          |                   |
|    | RespawnMessage   | VARCHAR  |        |          |                   |
|    | SafetyRange      | INTEGER  |        |          |                   |
|    | SafetyTime       | BIGINT   |        |          |                   |
|    | GroupName        | VARCHAR  |        |          |                   |
|    | WorldName        | VARCHAR  |        |          |                   |
|    | WorldUidMsb      | BIGINT   |        |          |                   |
|    | WorldUidLsb      | BIGINT   |        |          |                   |
|    | X                | DOUBLE   |        |          |                   |      
|    | Y                | DOUBLE   |        |          |                   |
|    | Z                | DOUBLE   |        |          |                   |   
|    | Yaw              | FLOAT    |        |          |                   |
|    | Pitch            | FLOAT    |        |          |                   |
|    | Created          | DATETIME |        |          |                   |
|    | CreatorUidMsb    | BIGINT   |        |          |                   | 
|    | CreatorUidLsb    | BIGINT   |        |          |                   |
