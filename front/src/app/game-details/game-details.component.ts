import {
  Component,
  computed,
  effect,
  inject,
  Injector,
  resource,
  signal,
} from '@angular/core';
import { DefaultComponent } from '../abstract-default.component';
import { ShardTokensComponent } from '../../atomic-design/tokens/shards/shard-tokens.component';
import { FlagColor, Game, Player, Token } from '../types';
import { TokenComponent } from '../../atomic-design/tokens/token/token.component';
import { InfluenceTokensComponent } from '../../atomic-design/tokens/influences/influence-tokens.component';
import { ButtonComponent } from '../../atomic-design/button/button.component';
import { api } from '../http.service';
import { WithoutMyPlayerPipe } from './without-my-player.pipe';
import { FlagComponent } from '../../atomic-design/flag/flag.component';

@Component({
  selector: 'ins-game-details',
  imports: [
    ShardTokensComponent,
    TokenComponent,
    InfluenceTokensComponent,
    ButtonComponent,
    WithoutMyPlayerPipe,
    FlagComponent,
  ],
  templateUrl: './game-details.component.html',
  styleUrl: './game-details.component.scss',
})
export class GameDetailsComponent extends DefaultComponent {
  injector = inject(Injector);

  game = signal<Game | undefined>(this.httpService.currentGame());

  myPlayer = signal<Player | undefined>(undefined);
  nbRedFlags = signal<number>(0);
  nbBlackFlags = signal<number>(0);

  constructor() {
    super();
    effect(() => {
      const game = this.game();
      if (!game) return;
      this.myPlayer.set(game.players.find((player) => player.me));
      this.nbRedFlags.set(
        game.flags.filter((flag) => flag.color === 'RED').length,
      );
      this.nbBlackFlags.set(
        game.flags.filter((flag) => flag.color === 'BLACK').length,
      );
    });

    effect(() => {
      this.game.set(this.httpService.currentGame());
    });
  }

  giveToken(player: Player) {
    resource({
      loader: async () => {
        const game = await this.httpService.sweetFetch<Game, void>(
          api(`games/${this.game()?.id}/players/${player.id}/tokens`),
          'POST',
        );
        this.game.set(game);
        return game;
      },
      injector: this.injector,
    });
  }

  giveShardToken() {
    resource({
      loader: async () => {
        return this.httpService.sweetFetch<Game, void>(
          api(`games/${this.game()?.id}/tokens`),
          'POST',
        );
      },
      injector: this.injector,
    });
  }

  canGiveShardToken(): boolean {
    return (
      this.myPlayer()?.playableTokens.some((token) => token.type === 'SHARD') ??
      false
    );
  }

  canGiveInfluenceToken(playerId: number): boolean {
    return (
      this.myPlayer()
        ?.playableTokens.map((token) => token.owner)
        .filter((owner) => owner !== undefined && owner !== null)
        .some(
          (owner) => owner.id === playerId || owner.id === this.myPlayer()?.id,
        ) ?? false
    );
  }

  addShardToken() {
    resource({
      loader: async () => {
        return this.httpService.sweetFetch<Game, void>(
          api(`games/${this.game()?.id}/tokens/add`),
          'POST',
        );
      },
      injector: this.injector,
    });
  }

  addFlag(flagColor: FlagColor) {
    resource({
      loader: async () => {
        return this.httpService.sweetFetch<Game, FlagColor>(
          api(`games/${this.game()?.id}/flags`),
          'POST',
          flagColor,
        );
      },
      injector: this.injector,
    });
  }
}
