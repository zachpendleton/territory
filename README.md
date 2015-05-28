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

### POST /api/games/{id}/moves

Play a tile in an in-progress game.

## License

Copyright © 2015 Zach Pendleton

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
