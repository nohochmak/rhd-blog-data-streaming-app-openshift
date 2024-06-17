/* SystemJS module definition */
declare var module: NodeModule;

interface NodeModule {
  id: string;
}

declare type EmojiData = {
  emoji: string;
  count: number;
};

declare type TootData = {
  item1: string; // emoji
  item2: string; // json string with tweet content
};

declare type Account = {
  id: bigint;
  username: string;
  url: string;
  bot: boolean;
  followers_count: bigint;
  following_count: bigint;
  statuses_count: bigint;
};

declare type TootCard = {
  id: bigint;
  created_at: string;
  language: string;
  content: string;
  url: string;
  account: Account;
};

declare var MyEventSource: {
  new (url: string, eventSourceInitDict?: EventSourceInit): EventSource;
  prototype: EventSource;
};
