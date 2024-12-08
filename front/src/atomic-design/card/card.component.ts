import { Component, input } from '@angular/core';

@Component({
  selector: 'ins-card',
  imports: [],
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss',
})
export class CardComponent {
  title = input.required<string>();
  color = input<string>();
}
