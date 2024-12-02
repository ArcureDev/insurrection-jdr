import { Component, computed, inject, Injector, resource } from '@angular/core';
import { DefaultComponent } from '../abstract-default.component';
import { Game } from '../types';
import { api } from '../http.service';
import { CardComponent } from '../../atomic-design/card/card.component';
import { ButtonComponent } from '../../atomic-design/button/button.component';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PATH_USER } from '../app.routes';

@Component({
  selector: 'ins-game',
  imports: [CardComponent, ButtonComponent, ReactiveFormsModule],
  templateUrl: './game.component.html',
  styleUrl: './game.component.scss',
})
export class GameComponent extends DefaultComponent {
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);

  game = computed(() => this.httpService.currentGame());

  copyUrl() {
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
}
