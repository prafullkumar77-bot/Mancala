package com.offline.mancala;
import java.util.*;
public class GameTest {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  Game g=new Game();check(!g.move(7),"reject opponent pit");check(g.move(2)&&g.extra&&g.turn==0&&g.pits[6]==1,"extra turn");
  g=new Game();g.pits=new int[]{0,1,0,0,0,1,0,1,0,0,5,0,0,0};g.move(1);check(g.captured==6&&g.pits[6]==6&&g.pits[10]==0,"capture opposite");
  g=new Game();g.pits=new int[]{0,0,0,0,0,1,20,1,1,1,1,1,1,21};g.move(5);check(g.over&&g.pits[6]==21&&g.pits[13]==27,"end sweep");
  g=new Game();g.pits[0]=15;g.move(0);check(g.pits[13]==0,"skip opposing store");
  Random random=new Random(7);for(int match=0;match<200;match++){g=new Game();int turns=0;while(!g.over&&turns++<1000){List<Integer> moves=g.moves();check(!moves.isEmpty(),"legal moves");g.move(moves.get(random.nextInt(moves.size())));check(Arrays.stream(g.pits).sum()==48,"stone conservation");for(int n:g.pits)check(n>=0,"nonnegative");}check(g.over,"termination");check(g.pits[6]+g.pits[13]==48,"final stores");}
  g=new Game();long start=System.nanoTime();int move=Game.bot(g,true,100);check(g.moves().contains(move),"expert legal");check(System.nanoTime()-start<2000000000L,"bounded search");
  g=new Game();g.pits=new int[]{0,0,0,0,0,1,23,1,0,0,0,0,1,22};check(Game.bot(g,true,100)==5,"winning move");
  System.out.println("PASS: captures, extra turns, skipped store, end sweep, 200 full games, expert legality and timing.");
 }
}
