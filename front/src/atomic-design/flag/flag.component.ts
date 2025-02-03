import { Component, input } from '@angular/core';
import { FlagColor } from '../../app/types';

@Component({
  selector: 'ins-flag',
  imports: [],
  templateUrl: './flag.component.html',
  styleUrl: './flag.component.scss',
})
export class FlagComponent {
  flagColor = input.required<FlagColor>();
}
