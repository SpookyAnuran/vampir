
This is intended to be a very simple vampire mod that is multiplayer friendly. feel free to nab this and tweak/port it to your hearts desire! (this was originally a datapack, but due to broken or moved 1.21.1 hooks, was ported to fabric.) This is my first minecraft mod, so enjoy the spaghetti! 

Mechanics: 
- Vampire and human tagging system. Vampirism can be toggled with [ /vampir curevampire @p ] and [/vampir addvampire @p ] by operators. There is a ritual, and a bite/infection mechanic as well. You can see your status by doing the command [ /vampir status @p ] For thematic reasons, without an admin, there is no cure to this curse. 

- The bite attack vampires get is simple, you sprint at a player with both hands empty to trigger the attack. If it lands, there is a 50/50 chance you trigger the blood sickness. Feature is located in VampirInfection.java . Blood sickness is a potion effect. if you die or sleep with the effect, you turn into a vampire. buckets of milk and waiting the short timer out is how one keeps thier humanity! (bugfixed!)

- The ritual to become a vampire is also very simple, and is a placeholder. You will need a 2x2 black terracotta floor and a diamond. right click the platform with the diamond at night to become a biter... if you are a vampire already, the ritual will not fire, and thus will not eat your diamond. VampirRitual.java is the file with this feature's code. 

- Vampires rot in the sun. I chose a non-fire method of damaging a vampire in sunlight. This is to prevent many mods from giving a player fire immunity and bypassing this weakness, it also serves to give a nod to more classic folklore about vampires being based on misunderstood decomposition. fun fact: vampires burning in the sun was not a thing until the nosferatu film, it got adopted into most vampire lore after. (BUG fixed!) This file is separate from the others so forks and personal edits can swap out the effect easier, look for VampirSunlight.java 

- Vampires are weak to wooden swords. (implemented in Vampir.java, needs more testing) - Vampire boons. Vampires are faster when in the shadows. this feature is located in VampirBoon.java Vampires also do more damage with thier bare fist.

- Undead are neutral to vampires, and zombies will follow vampires up until a distance of 6 blocks. A clever vampire can use this mechanic to build an army... be careful not to hit them though, they do get grumpy (BUGFEATURE: It seems that after hitting a zombie, if a vampire player runs away, and later finds the zombie again without dying, it wont attack the player unless they are standing right next to them. We playfully call them passive aggressive zombies. This is hilarious, and we will not patch this on purpose.) (Needs cleanup: the only way I could find to implement undead neutrality was by disabling the attack action towards a vampire player every tick... as you can imagine, this is potentially very laggy. If you have suggestions on how to implement the above better, please reach out! I'm very new to this. VampirZombies.java is where this feature is located. also see the mixins folder.) 

- (needs implemented: Water breathing) this feature will be located in VampirBoon.java I need a lag free way to implement it continuously. Bare handed attacks to a boost to damage. hold an item to reduce your damage for the sake of feeding.(BUG fixed!) 

- Vampire diet As of now, Vampires can punch living things for hunger, or eat raw meat. mushroom stew and golden apples and carrots work for more reasons. they can also craft a blood bottle using three kinds of raw red meat for more hunger. (Bug: internal datapack will not load.)

- Vampires sleeping during the day & coffin item: Pending, will be added to VampirSleep.java. I also need to figure out how to make block items, and work on blockbench. Planned now: adding the ability to sleep in a bed during the day. i need help implementing this...
