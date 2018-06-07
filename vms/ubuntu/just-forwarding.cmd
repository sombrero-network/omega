rem configure sshd forwardings on host
rem "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet1 --port-forward-4 "ssh:tcp:[]:30001:[192.168.15.4]:22"
rem "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet2 --port-forward-4 "ssh:tcp:[]:30002:[192.168.15.4]:22"

rem remove forwardings requires to stop and start network to drop existing connections
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet1 --port-forward-4 delete ssh
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet2 --port-forward-4 delete ssh
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork stop --netname natnet1
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork start --netname natnet1
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork stop --netname natnet2
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork start --netname natnet2

rem by adding forwarding system processes for ports are created and no need to stop/start network individually to trigger that
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet1 --port-forward-4 "ssh:tcp:[]:30001:[192.168.15.4]:22"
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet2 --port-forward-4 "ssh:tcp:[]:30002:[192.168.15.4]:22"

