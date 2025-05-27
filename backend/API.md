# F1 Champions API Documentation

## Overview

The F1 Champions API provides access to Formula 1 World Champions and race results data. It serves as a wrapper around the Ergast API, providing a more focused and curated set of endpoints.

## Base URL

- Development: `http://localhost:8080`

## Authentication

Currently, the API is public and does not require authentication. However, rate limiting is in place to protect the underlying Ergast API.

## Rate Limits

- 4 requests per second
- 200 requests per hour

## Endpoints

### Champions

#### Get World Champions
```http
GET /api/seasons
```

Response:
```json
[
  {
    "year": 2005,
    "championName": "Fernando Alonso",
    "championPoints": 133,
    "championWins": 7
  },
  {
    "year": 2006,
    "championName": "Fernando Alonso",
    "championPoints": 134,
    "championWins": 7
  },
  {
    "year": 2007,
    "championName": "Kimi Räikkönen",
    "championPoints": 110,
    "championWins": 6
  },
  {
    "year": 2008,
    "championName": "Lewis Hamilton",
    "championPoints": 98,
    "championWins": 5
  },
  {
    "year": 2009,
    "championName": "Jenson Button",
    "championPoints": 95,
    "championWins": 6
  }
]
```

### Races

#### Get Race Results
```http
GET /api/seasons/{year}/races
```

Path Parameters:
- `year`: The season year (e.g., 2023)  

Response:
```json
[
  {
    "round": 1,
    "raceName": "Austrian Grand Prix",
    "date": "2020-07-05",
    "circuitName": "Red Bull Ring",
    "winningDriverName": "Valtteri Bottas",
    "winningDriverNationality": "Finnish",
    "winningConstructorName": "Mercedes",
    "isSeasonChampionWinner": false
  },
  {
    "round": 2,
    "raceName": "Styrian Grand Prix",
    "date": "2020-07-12",
    "circuitName": "Red Bull Ring",
    "winningDriverName": "Lewis Hamilton",
    "winningDriverNationality": "British",
    "winningConstructorName": "Mercedes",
    "isSeasonChampionWinner": true
  },
  {
    "round": 3,
    "raceName": "Hungarian Grand Prix",
    "date": "2020-07-19",
    "circuitName": "Hungaroring",
    "winningDriverName": "Lewis Hamilton",
    "winningDriverNationality": "British",
    "winningConstructorName": "Mercedes",
    "isSeasonChampionWinner": true
  },
  {
    "round": 4,
    "raceName": "British Grand Prix",
    "date": "2020-08-02",
    "circuitName": "Silverstone Circuit",
    "winningDriverName": "Lewis Hamilton",
    "winningDriverNationality": "British",
    "winningConstructorName": "Mercedes",
    "isSeasonChampionWinner": true
  }
]
```

## Error Responses

The API uses standard HTTP status codes:

- `200 OK`: Request successful
- `400 Bad Request`: Invalid parameters
- `404 Not Found`: Resource not found
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

Error Response Format:
```json
{
  "timestamp": "2024-03-26T13:33:25.767Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid year parameter",
  "path": "/api/v1/champions"
}
```

## Data Models

### Driver
```json
{
  "driverId": "string",
  "code": "string",
  "firstName": "string",
  "lastName": "string",
  "nationality": "string"
}
```

### Constructor
```json
{
  "constructorId": "string",
  "name": "string",
  "nationality": "string"
}
```

### Circuit
```json
{
  "circuitId": "string",
  "name": "string",
  "location": "string",
  "country": "string"
}
```

## Support

For API support or to report issues:
- GitHub Issues: [https://github.com/yourusername/f1-champions-assesment/issues](https://github.com/yourusername/f1-champions-assesment/issues)
- Email: canberk.ozcelik@gmail.com

## License

This API is licensed under the MIT License. See the [LICENSE](../LICENSE) file for details. 