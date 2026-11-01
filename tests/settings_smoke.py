import subprocess,xml.etree.ElementTree as ET,re,os
adb=os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
def run(*a):return subprocess.check_output([adb,*a])
def nodes():
 run('shell','uiautomator','dump','/sdcard/mancala.xml')
 ns=list(ET.fromstring(run('shell','cat','/sdcard/mancala.xml')).iter('node'))
 assert not any(n.get('resource-id')=='android:id/alertTitle' for n in ns)
 return ns
def tap(prefix):
 n=next(n for n in nodes() if n.get('text','').startswith(prefix));x,y,r,b=map(int,re.findall(r'\d+',n.get('bounds')));run('shell','input','tap',str((x+r)//2),str((y+b)//2))
def has(prefix):return any(n.get('text','').startswith(prefix) for n in nodes())
def shot(name):
 with open(os.path.expanduser('~/Documents/MancalaAndroid/build/'+name+'.png'),'wb') as f:f.write(run('exec-out','screencap','-p'))
assert not has('THE CLASSIC STONE GAME');shot('home-centered')
tap('Settings');shot('settings-custom')
tap('Music');assert has('Music  ·  Off')
tap('Sound effects');assert has('Sound effects  ·  Off')
tap('Rate us');assert has('Mancala Offline is not on Google Play yet.')
tap('Back to settings');tap('×')
tap('Play solo');shot('difficulty-custom');tap('Easy')
if has('Start a fresh game?'):tap('Start game')
tap('?');assert has('A handful of rules');shot('rules-custom');tap('Let’s play')
tap('Settings');tap('New game');assert has('Start again?');tap('Keep playing')
run('shell','am','force-stop','com.offline.mancala');run('shell','am','start','-n','com.offline.mancala/.MainActivity')
tap('Settings');assert has('Music  ·  Off');assert has('Sound effects  ·  Off')
tap('Music');tap('Sound effects');tap('×')
print('PASS: centered home; custom difficulty, rules, restart, settings and rating dialogs; audio toggle persistence.')
