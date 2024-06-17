import { animate, style, transition, trigger } from '@angular/animations';
import { AfterContentInit, Component, Input } from '@angular/core';
import { parse } from 'json-bigint';
@Component({
  selector: 'app-tweet-card',
  templateUrl: './toot-card.component.html',
  styleUrls: ['./toot-card.component.scss'],
  animations: [
    trigger('incoming', [
      transition(':enter', [
        style({ transform: 'translateX(300%)', maxHeight: '0' }),
        animate(
          200,
          style({ transform: 'translateX(150%)', maxHeight: '30rem' })
        ),
        animate(400)
      ])
    ])
  ]
})
export class TootCardComponent
 implements AfterContentInit  {
  
  @Input() tootData:TootData;
  tootCard:TootCard;

  constructor() { }
  
  ngAfterContentInit(): void {
    this.tootCard = parse(this.tootData.item2);
  }

}
