# gopher-game
Turn based gopher guessing game using Android/Java threads. The game picks the tile to opened based on player hueristics. 
Player 1 will keep choosing tiles randomly until it encounters a tile that seem closer to the gopher vicinity, then it starts to choose 
tiles only from that close vicinity set of tiles.
Player 2 has a more naive way of doing things, it will always choose random tiles even if it encounters a tile which is extremely close to
the gopher.

Game comes with two modes.
1. Control Mode
  The UI has two buttons for Player 1 and Player 2. 
2. Continuous Mode
  The game will automatically alternate choosing tiles for Player 1 and Player 2.
  
Android Async Tasks, Loopers, Message queues, Runnables where used to implement this game. 


