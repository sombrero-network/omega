rem stop the machines
rem https://www.virtualbox.org/manual/ch08.html#vboxmanage-controlvm
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" controlvm ubuntu_16.04_x64 poweroff
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" controlvm ubuntu_16.04_x64-1 poweroff

rem get new uuid for existing vdi if lost
rem "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" internalcommands sethduuid ubuntu_16.04_x64.vdi


rem remove nat networks
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet1
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet2
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet3
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet4
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet5
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet6
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet7
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet8
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet9
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork remove --netname natnet10

rem create nat network for each vm
rem https://www.virtualbox.org/manual/ch06.html#network_nat_service
rem https://www.virtualbox.org/manual/ch08.html
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet1 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet2 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet3 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet4 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet5 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet6 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet7 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet8 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet9 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork add --netname natnet10 --network "192.168.15.0/24" --enable --ipv6 on --dhcp on

rem once vm was downloaded add it to vbox and set networking
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" unregistervm ubuntu_16.04_x64
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" registervm "C:\repos\omega-governance-sombero\vms\ubuntu\ubuntu_16.04_x64.vbox"

rem once machine was added to vbox after download with its uuid; create a clone of this vm to get new uuid
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" unregistervm ubuntu_16.04_x64-1 --delete
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" clonevm ubuntu_16.04_x64 --name ubuntu_16.04_x64-1 --basefolder "C:\repos\omega-governance-sombero\vms\ubuntu-1" --register

rem set networking nat for cloned vm
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" modifyvm ubuntu_16.04_x64 --nat-network1 natnet3
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" modifyvm ubuntu_16.04_x64-1 --nat-network1 natnet2

rem configure sshd binding on host
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet3 --port-forward-4 "ssh:tcp:[]:30001:[192.168.15.4]:22"
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet3 --port-forward-4 "ssh1:tcp:[]:30001:[192.168.15.5]:22"
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet2 --port-forward-4 "ssh:tcp:[]:30002:[192.168.15.4]:22"
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" natnetwork modify --netname natnet2 --port-forward-4 "ssh1:tcp:[]:30002:[192.168.15.5]:22"

rem start the machines
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" startvm ubuntu_16.04_x64
"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" startvm ubuntu_16.04_x64-1