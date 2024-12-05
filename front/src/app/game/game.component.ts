import {
  Component,
  computed,
  ElementRef,
  inject,
  Injector,
  resource,
  viewChild,
} from '@angular/core';
import { DefaultComponent } from '../abstract-default.component';
import { Game } from '../types';
import { api } from '../http.service';
import { CardComponent } from '../../atomic-design/card/card.component';
import { ButtonComponent } from '../../atomic-design/button/button.component';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { authenticatedRoute, PATH_USER } from '../app.routes';
import { ShardTokensComponent } from '../../atomic-design/tokens/shards/shard-tokens.component';
import { InfluenceTokensComponent } from '../../atomic-design/tokens/influences/influence-tokens.component';

@Component({
  selector: 'ins-game',
  imports: [
    CardComponent,
    ButtonComponent,
    ReactiveFormsModule,
    RouterLink,
    ShardTokensComponent,
    InfluenceTokensComponent,
  ],
  templateUrl: './game.component.html',
  styleUrl: './game.component.scss',
})
export class GameComponent extends DefaultComponent {
  dealTokensDialog =
    viewChild<ElementRef<HTMLDialogElement>>('dealTokensDialog');

  private readonly router = inject(Router);
  private readonly injector = inject(Injector);

  game = computed(() => this.httpService.currentGame());

  protected readonly authenticatedRoute = authenticatedRoute;

  copyUrl() {
    console.log('coucou', this.game()?.url);
    navigator.clipboard.writeText(
      this.game()?.url ?? "LA GAME N'EXISTE PAS D:",
    );
  }

  closeGame() {
    if (!this.game()) return;
    resource({
      loader: async () => {
        fetch(api(`games/${this.game()?.id}`), {
          method: 'DELETE',
        }).then(() => {
          this.httpService.currentGame.set(undefined);
          this.router.navigate([PATH_USER]);
        });
      },
      injector: this.injector,
    });
  }

  dealTokens() {
    if (!this.game()) return;
    resource({
      loader: async () => {
        const game = await this.httpService.sweetFetch<Game, void>(
          api(`games/${this.game()?.id}/tokens`),
        );
        this.httpService.currentGame.set(game);
        this.dealTokensDialog()?.nativeElement.close();
      },
      injector: this.injector,
    });
  }
}
