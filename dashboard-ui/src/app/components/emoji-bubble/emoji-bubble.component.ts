import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-emoji-bubble',
  templateUrl: './emoji-bubble.component.html',
  styleUrls: ['./emoji-bubble.component.scss']
})
export class EmojiBubbleComponent {
  @Input() emojiData: EmojiData;
  @Input() ranking: number;

  constructor() {}

  getRankedClass() {
    switch (this.ranking) {
      case 0:
        return 'first';
      case 1:
        return 'second';
      case 2:
        return 'third';
    }
    return null;
  }
}
