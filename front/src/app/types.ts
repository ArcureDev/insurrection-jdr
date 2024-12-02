export type Credentials = {
  username: string;
  password: string;
};

export type GameState = 'ON_GOING' | 'DONE';

export type Player = {
  id: string;
  name: string;
};

export type Game = {
  id: string;
  state: GameState;
  players: Player[];
  url: string;
};
