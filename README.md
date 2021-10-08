# Apache Nifi Custom SMPP Processor

##### Problem faced  <br>
We were trying to read data from an internal system supporting only SMPP protocal unfortunately, Apache Nifi doesn't by <br> 
default ship with any SMPP client processor. <br>

Tasks 
- Building processor that can connect via SMPP protocal 
- Package the data as JSON
- TPS of >=8000


##### Solution
We are thinking of having the processor having capabilities of 
- handling of executors
- handling of multiple threads (and cleanups)
- handling of rebind in case of lost connection
- handling of enquire_link to keep connection alive in periods of inactivity
- handling of queuing and windowing for async messaging.

