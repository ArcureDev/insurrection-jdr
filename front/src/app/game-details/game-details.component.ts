import { Component, computed, effect, signal } from '@angular/core';
import { DefaultComponent } from '../abstract-default.component';
import { ShardTokensComponent } from '../../atomic-design/tokens/shards/shard-tokens.component';
import { Player } from '../types';
import { TokenComponent } from '../../atomic-design/tokens/token/token.component';
import { InfluenceTokensComponent } from '../../atomic-design/tokens/influences/influence-tokens.component';

@Component({
  selector: 'ins-game-details',
  imports: [ShardTokensComponent, TokenComponent, InfluenceTokensComponent],
  templateUrl: './game-details.component.html',
  styleUrl: './game-details.component.scss',
})
export class GameDetailsComponent extends DefaultComponent {
  game = computed(() => this.httpService.currentGame());

  myPlayer = signal<Player | undefined>(undefined);

  constructor() {
    super();
    effect(() => {
      const game = this.game();
      if (!game) return;
      this.myPlayer.set(game.players.find((player) => player.me));
    });
  }
}
