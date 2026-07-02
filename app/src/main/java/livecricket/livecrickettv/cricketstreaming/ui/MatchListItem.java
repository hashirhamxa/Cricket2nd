package livecricket.livecrickettv.cricketstreaming.ui;

import livecricket.livecrickettv.cricketstreaming.data.model.Match;

public abstract class MatchListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_MATCH = 1;

    public abstract int getType();

    public static class HeaderItem extends MatchListItem {
        private final String title;
        public HeaderItem(String title) { this.title = title; }
        public String getTitle() { return title; }
        @Override public int getType() { return TYPE_HEADER; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeaderItem that = (HeaderItem) o;
            return java.util.Objects.equals(title, that.title);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(title);
        }
    }

    public static class MatchItem extends MatchListItem {
        private final Match match;
        public MatchItem(Match match) { this.match = match; }
        public Match getMatch() { return match; }
        @Override public int getType() { return TYPE_MATCH; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MatchItem matchItem = (MatchItem) o;
            return java.util.Objects.equals(match, matchItem.match);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(match);
        }
    }
}
