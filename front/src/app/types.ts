export type Credentials = {
  username: string;
  password: string;
};

export type GameState = 'START' | 'ON_GOING' | 'DONE';
export type TokenType = 'SHARD' | 'INFLUENCE';
export type PlayerRole =
  | 'POUVOIR'
  | 'ORDRE'
  | 'ECHO'
  | 'PEUPLE'
  | 'PAMPHLET'
  | 'MOLOTOV'
  | 'ECUSSON'
  | 'ETOILE';

export type PlayerPayload = {
  name: string;
  color: string;
};

export type Player = PlayerPayload & {
  id: number;
  role: PlayerRole;
  playableTokens: Token[];
  myTokens: Token[];
  me: boolean;
};

export type SimplePlayer = PlayerPayload & {
  id: number;
};

export type Color = {
  color: string;
};

export type Game = {
  id: string;
  state: GameState;
  players: Player[];
  nbAvailableShardTokens: number;
  url: string;
  flags: Flag[];
};

export type User = {
  id: number;
  username: string;
};

export type Token = {
  id: number;
  type: TokenType;
  owner?: Player | null;
  player: Player;
};

export type Flag = {
  id: number;
  color: FlagColor;
  player: SimplePlayer;
  date: Date;
};

export type FlagColor = 'RED' | 'BLACK';
