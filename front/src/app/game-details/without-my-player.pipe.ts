import { Pipe, PipeTransform } from '@angular/core';
import { Player } from '../types';

@Pipe({
  name: 'withoutMyPlayer',
})
export class WithoutMyPlayerPipe implements PipeTransform {
  transform(players?: Player[], myPlayer?: Player): Player[] {
    return players?.filter((player) => player.id !== myPlayer?.id) ?? [];
  }
}
