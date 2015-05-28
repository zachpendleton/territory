# Territory

A game server. Fun!

## API

### GET /api/games/{id}

Fetch the status of a previously created game.

#### Response

```json
{
  "players":[],
  "state": "initiating",
  "rows": 6,
  "cols": 6,
  "draw_size": 36,
  "claims": [],
  "player_id": null
}
```

### POST /api/games

Create a new Game object.

#### Request

```json
{
  "rows": 6,
  "cols": 6
}
```

#### Response

An empty 201 with a game URL in the `Location` header.

### POST /api/games/{id}/players

Create a new player inside a game.

#### Request

```json
{
  "name": "Don Draper"
}
```

#### Response

```json
{
  "players":[
    {
      "name": "Don Draper",
      "score": 0,
      "id": "a7397ddf",
      "hand":[
        {"row":3,"col":1},
        {"row":1,"col":5},
        ...
      ],
    },

    {
      "name": "Peggy Olson",
      "score": 0,
      "id" a1d6f35a",
      "hand": 6
    }
  ],
  "state": "in play",
  "rows": 6,
  "cols": 6,
  "draw_size":24,
  "claims": [],
  "player_id": "a7397ddf"
}
```

Response will also include a token in the `X-Turn-Token` that must be included
in the `X-Turn-Token` header in the subsequent call to the `moves` endpoint.

### POST /api/games/{id}/moves

Play a tile in an in-progress game.

#### Request

NOTE: Requests _must_ include the `X-Turn-Token` header from the previous turn.

```json
{
  "row": 3,
  "col": 1
}
```

To skip the current team, the player may send a body of "PASS" to the server.

#### Response

```json
{
  "players": [
    {
      "name": "Peggy Olson",
      "score": 2,
      "id": "c65f9efc",
      "hand": [
        {"row": 4, "col": 0},
        {"row": 0, "col": 0},
        ...
      ],
    },
    {
      "name": "Don Draper",
      "score": 1,
      "id": "890a9f3b",
      "hand": 6
    }
  ],
  "state": "in play",
  "draw_size": 10,
  "claims": [
    {
      "tile": {"row": 3, "col": 1},
      "owner": "c65f9efc"
    }
  ]
}
```

## License

Copyright © 2015 Zach Pendleton

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
