import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { EmojiBubbleComponent } from './components/emoji-bubble/emoji-bubble.component';
import { LiveCountComponent } from './components/live-count/live-count.component';
import { WrapperComponent } from './components/wrapper/wrapper.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TopNComponent } from './components/top-n/top-n.component';
import { SliceCollectionPipe } from './pipes/slice-collection.pipe';
import { SortEmojiPipe } from './pipes/sort-emoji.pipe';
import { TootStreamComponent } from './components/toot-stream/toot-stream.component';
import { TootCardComponent } from './components/toot-card/toot-card.component';

@NgModule({
  declarations: [
    AppComponent,
    EmojiBubbleComponent,
    LiveCountComponent,
    WrapperComponent,
    TopNComponent,
    SliceCollectionPipe,
    SortEmojiPipe,
    TootStreamComponent,
    TootCardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FontAwesomeModule,
    BrowserAnimationsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
