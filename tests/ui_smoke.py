import subprocess,xml.etree.ElementTree as ET,re,time,os
adb=os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
def run(*a):return subprocess.check_output([adb,*a],text=True)
def nodes():
 run('shell','uiautomator','dump','/sdcard/mancala.xml')
 return list(ET.fromstring(run('shell','cat','/sdcard/mancala.xml')).iter('node'))
def tap(n):
 x,y,r,b=map(int,re.findall(r'\d+',n.get('bounds')));run('shell','input','tap',str((x+r)//2),str((y+b)//2))
def select(prefix):tap(next(n for n in nodes() if n.get('text','').casefold().startswith(prefix.casefold())))
def start():
 ns=nodes()
 if any(n.get('text','').casefold()=='start game' for n in ns):tap(next(n for n in ns if n.get('text','').casefold()=='start game'))
 time.sleep(.8)
def pit(index):
 n=next(n for n in nodes() if n.get('content-desc','').startswith('Mancala board.'))
 l,t,r,b=map(int,re.findall(r'\d+',n.get('bounds')));sx=(r-l)/1000;sy=(b-t)/420;x=l+(209+116*index)*sx;y=t+295*sy
 run('shell','input','tap',str(int(x)),str(int(y)))
select('Two Players');start();pit(2);time.sleep(2.5)
assert any('Go again' in n.get('text','') for n in nodes())
pit(0);time.sleep(2.5)
ns=nodes();assert any('Player 2’s turn' in n.get('text','') for n in ns)
before=next(n.get('content-desc') for n in ns if n.get('content-desc','').startswith('Mancala board.'))
select('‹ Home');select('Continue game')
after=next(n.get('content-desc') for n in nodes() if n.get('content-desc','').startswith('Mancala board.'))
assert before==after
select('‹ Home');select('Play solo');select('Expert');start();pit(0);time.sleep(8)
assert any('Your turn' in n.get('text','') for n in nodes())
select('‹ Home')
print('PASS: home modes, landscape board, extra turn, two-player handoff, resume, Expert animated reply.')
