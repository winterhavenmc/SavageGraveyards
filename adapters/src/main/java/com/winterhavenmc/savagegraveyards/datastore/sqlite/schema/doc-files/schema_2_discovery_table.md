
# Schema Version 2

## Discovery Table
```(sql)
CREATE TABLE Discovery
    (GraveyardKey INTEGER NOT NULL REFERENCES Graveyard(Key) ON DELETE CASCADE,
    PlayerUidMsb BIGINT NOT NULL,
    PlayerUidLsb BIGINT NOT NULL,
    Timestamp DATETIME,
PRIMARY KEY (GraveyardKey, PlayerUidMsb, PlayerUidLsb));
```

| pk | name         | type      | null? |
|----|--------------|-----------|-------|
| ✓  | GraveyardKey | INTEGER   |       |
|    | PlayerUidMsb | BIGINT    |       |
|    | PlayerUidLsb | BIGINT    |       |
|    | Timestamp    | DATETIME  | ✓     |
