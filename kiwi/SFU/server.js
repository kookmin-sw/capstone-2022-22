var WebSocketServer = require('ws').Server;
var wss = new WebSocketServer( {port: 5100} );

var mediasoup = require('mediasoup');
var express = require('express');
var socket = require('socket.io');


const app = express();
const PORT = 4000;
let server = app.listen(PORT);

let worker;
let router;

let producer_dict = {};
let username_dict = {};
let producer_transport_dict = {};

let consumer_dict = {};
let consumer_transport_dict = {};
let socketIdList = {};

let usernameBySocket = {};
let interviewroomPlayers = {};
let restroomPlayers = {};

function getQueryStringObject(url) {
    // url = window.location.search;
    var query = {};
    var pairs = (url[0] === '?' ? url.substr(1) : url).split('&');
    for (var i = 0; i < pairs.length; i++) {
        var pair = pairs[i].split('=');
        query[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1] || '');
    }
    return query;;
}

class Clients {
    constructor() {
        this.clientList = [[], [], []]; // 0 -> restroom 1 -> interview 2 -> username
        this.saveClient = this.saveClient.bind(this);
    }

    saveClient(join, client, url) {
        // console.log(url)
        var splited_url = getQueryStringObject(url)
        console.log(splited_url.username)
        // url = "jinwoo"
        if(join == "restroom"){
            this.clientList[0].push(client)
        }
        else if(join == "interview"){
            this.clientList[1].push(client)
            // url = "sohyeon"
            // var userSocket = usernameBySocket[url];
            console.log(splited_url.username)
            var userSocket = usernameBySocket[splited_url.username];
            console.log("INTERVIEW")
            console.log(userSocket)
            userSocket.emit("start",{socketId: userSocket.id})
        }
        // this.clientList[2].push(splited_url.username)
        this.clientList[2].push(url)
    }
}

const clients = new Clients();

// function updatePlayers(room,player_id,x,y,z){
//     if(room == "interview"){
//         interviewroomPlayers[player_id].x = x;
//         interviewroomPlayers[player_id].y = y;
//         interviewroomPlayers[player_id].z = z;
//     }
//     if(room == "restroom"){
//         restroomPlayers[player_id].x = x;
//         restroomPlayers[player_id].y = y;
//         restroomPlayers[player_id].z = z;
//     }
// }

// function createPlayer(room, data,player_id){
//     var bodyColor = data["bodyColor"]
//     var hairColor = data["hairColor"]
//     var topColor = data["topColor"]
//     var bottomColor = data["bottomColor"]
//     var hairStyle = data["hairStyle"]
//     var topStyle = data["topStyle"]
//     var bottomStyle = data["bottomStyle"]
//     var shoesStyle = data["shoesStyle"]
//     var x = data["x"]
//     var y = data["y"]
//     var z = data["z"]

//     const player = new Player();
//     player.bodyColor = bodyColor
//     player.hairColor = hairColor
//     player.topColor = topColor
//     player.bottomColor = bottomColor
//     player.hairStyle = hairStyle
//     player.topStyle = topStyle
//     player.bottomStyle = bottomStyle
//     player.shoesStyle = shoesStyle
//     player.x = x
//     player.y = y
//     player.z = z

//     result = []
//     if(room == "interview"){
//         for(var ids in interviewroomPlayers){
//             console.log(ids);
//             const anotherPlayer = interviewroomPlayers[ids]
//             const data = {
//                 room : "interview",
//                 command: "NEW_PLAYER",
//                 player_id : ids,
//                 data : {
//                     bodyColor : anotherPlayer.bodyColor,
//                     hairColor : anotherPlayer.hairColor,
//                     topColor : anotherPlayer.topColor,
//                     bottomColor : anotherPlayer.bottomColor,
//                     shoesColor : anotherPlayer.shoesColor,
//                     hairStyle : anotherPlayer.hairStyle,
//                     topStyle : anotherPlayer.topStyle,
//                     bottomStyle : anotherPlayer.bottomStyle,
//                     shoesStyle : anotherPlayer.shoesStyle,
//                     x : anotherPlayer.x,
//                     y : anotherPlayer.y,
//                     z : anotherPlayer.z
//                 }
//             }
//             result.push(data);
//         }
//         interviewroomPlayers[player_id] = player
//     }
//     if(room == "restroom"){
//         for(var ids in restroomPlayers){
//             console.log(ids);
//             const anotherPlayer = restroomPlayers[ids]
//             console.log(anotherPlayer)
//             const data = {
//                 room : "restroom",
//                 command: "NEW_PLAYER",
//                 player_id : ids,
//                 data : {
//                     bodyColor : anotherPlayer.bodyColor,
//                     hairColor : anotherPlayer.hairColor,
//                     topColor : anotherPlayer.topColor,
//                     bottomColor : anotherPlayer.bottomColor,
//                     shoesColor : anotherPlayer.shoesColor,
//                     hairStyle : anotherPlayer.hairStyle,
//                     topStyle : anotherPlayer.topStyle,
//                     bottomStyle : anotherPlayer.bottomStyle,
//                     shoesStyle : anotherPlayer.shoesStyle,
//                     x : anotherPlayer.x,
//                     y : anotherPlayer.y,
//                     z : anotherPlayer.z
//                 }
//             }
//             result.push(data);
//         }
//         restroomPlayers[player_id] = player
//     }

//     return result
// }

wss.on("connection",function(ws){
    ws.room = []

    //message 수집.
    ws.on('message',function(msg){
        var parsed_msg = JSON.parse(msg);
        var command = parsed_msg["command"]
        var player_id = parsed_msg["player_id"]
        var data = parsed_msg["data"]
        var room = parsed_msg["room"]
        console.log("Room : ", room);
        console.log("Command : ",command);
        console.log("Player ID : ",player_id);
        console.log("Data : ",data["x"],data["y"],data["z"])
        console.log(parsed_msg)
        
        if(parsed_msg.join){
            clients.saveClient(parsed_msg.join,ws,data["url"]);
            // player_list = createPlayer(room,data,player_id);
            // for(var player in player_list)
            //     ws.send(player)
        }
        // updatePlayers(room,player_id,data["x"],data["y"],data["z"])
        broadcast(parsed_msg);
    })

    ws.on('close', function() {
        console.log("Client Closed");
    })
})

wss.broadcast = function(data) {
    wss.clients.forEach(client =>{
        client.send(data)   
    })
}

function broadcast(msg){
    if(msg.room == "restroom"){
        for(var i = 0; i < clients.clientList[0].length; i++){
            var client = clients.clientList[0][i];
            client.send(JSON.stringify(msg))
        }
    } else if (msg.room == "interview") {
        for(var i = 0; i < clients.clientList[1].length; i++){
            var client = clients.clientList[1][i];
            client.send(JSON.stringify(msg))
        }
    }
}

(async() => {
    try{
        await runMediasoupWorker();
    }catch(err){
        console.log(err);
    }
})();

const io = socket(server, {
    allowEIO3: true,
    cors: {
        origin: true,
        credentials: true
    },
});

io.on("connection",(socket) => {
    console.log("socket ID : ",socket.id);
    socket.emit("connection-success",{
        socketId: socket.id
    })

    socket.on("saveUsername",async(data) => {
        console.log("SAVE USERNAME",data)
        usernameBySocket[data.username] = socket
    })

    socket.on("broadcastProducer",() => {
        socket.broadcast.emit("createConsumer",{producerId : producer_dict[socket.id].id, sender: socket.id})
    })

    socket.on("getRtpCapabilities",async(data,callback) => {
        console.log("Sending Rtp Capabilities...")
        console.log(data.username)
        username_dict[socket.id] = data
        if(socketIdList[socket.id] == true){
            callback({rtpCapabilities : false})    
        }
        socketIdList[socket.id] = true;
        callback({rtpCapabilities : router.rtpCapabilities})
        console.log("Rtp Capabilties Sended");
    })

    socket.on("createProducerTransport",async(data,callback) =>{
        try{
            console.log(producer_transport_dict[socket.id])
            if(producer_transport_dict[socket.id]){
                callback({error: "exists"})
            }
            console.log("Creating Producer Transport...");
            const { transport, params } = await createWebRtcTransport();
            producer_transport = transport;
            console.log("Producer Transport ID : ",producer_transport.id)
            callback(params);

            producer_transport_dict[socket.id] = transport;
            console.log("Succefully created Producer Transport");
        } catch(err){
            console.log("error",err);
            callback({error : err.message })
        }
    })

    socket.on("connectProducerTransport",async(data, callback) => {
        console.log("Connecting Producer Transport...");
        const producer_transport = producer_transport_dict[socket.id]
        await producer_transport.connect({ dtlsParameters:data.dtlsParameters });
        callback();
    })

    socket.on("produce", async(data,callback) => {
        const { kind, rtpParameters} = data;
        producer = await producer_transport.produce({ kind, rtpParameters });
        producer_dict[socket.id] = producer

        console.log("PRODUCER : ")
        callback({ id : producer.id });
    })

    socket.on("createConsumerTransport",async(data,callback)=>{
        try{
            const { transport, params } = await createWebRtcTransport();
            console.log("NEW CONSUMER TRANSPORT");
            console.log(transport.id);
            console.log("SENDER")
            console.log(username_dict[data.sender])
            
            try{
                consumer_transport_dict[socket.id].push(transport);
            }catch(error){
                consumer_transport_dict[socket.id] = [transport]
            }
            callback({params : params, username : username_dict[data.sender]});
        }catch(err){
            console.error(err);
            callback({ error : err})
        }
    })

    socket.on("createConsumerTransportList",async(data,callback) => {
        result = []
        transport_list = []
        try{
            for(var key in producer_dict){
                const { transport,params } = await createWebRtcTransport()
                console.log("New Consumer Transport ID : ",transport.id);
                
                result.push([params, producer_dict[key].id, username_dict[key]])
                transport_list.push(transport);
            }
            consumer_transport_dict[socket.id] = transport_list;
            callback(result);
        }catch(err){
            console.log(err);
            callback({ error : err.message });
        }
    })

    socket.on("connectConsumerTransportList",async(data, callback) => {
        console.log("Connecting Consumer Transport ID : ",data.transportId);
        
        let consumer_transport;
        console.log("CONUSMER LENGTH : ",consumer_transport_dict[socket.id].length)
        for(let i = 0; i < consumer_transport_dict[socket.id].length; i++){
            if(consumer_transport_dict[socket.id][i].id == data.transportId){
                consumer_transport = consumer_transport_dict[socket.id][i];
                break;
            }
        }

        await consumer_transport.connect({ dtlsParameters: data.dtlsParameters });
        callback();
    })

    socket.on("resume", async(data, callback) => {
        id = data.consumer_id;
        let consumer;
        console.log("data : ",id);
        for(let i = 0; i < consumer_dict[socket.id].length; i++){
            console.log("consumer_dict id : ",consumer_dict[socket.id][i].id);
            if(consumer_dict[socket.id][i].id == id){
                consumer = consumer_dict[socket.id][i];
                break;
            }
        }
        console.log("RESUMING CONSUMER : ");
        console.log(consumer.id);
        await consumer.resume();
        callback();
    })

    socket.on("consume", async(data,callback) =>{
        console.log("============ CONSUME ============");
        console.log(data.producerId);
        const result = await createConsumer(data.rtpCapabilities, data.id, data.producerId, socket.id)
        callback(result);
    })

    socket.on("disconnect",() => {
        console.log("socket disconnected : ",socket.id);
        
        // delete consumer;
        // consumer_dict[socket.id].close();
        // consumer_transport_dict[socket.id].close();
        var i;
        // for(i = 0; i < consumer_dict[socket.id].length; i++){
        //     console.log("consumer id : ",consumer_dict[socket.id][i].id)
        //     consumer_dict[socket.id][i].close();
        // }
        if(consumer_dict[socket.id]){
            console.log("consumer id : ",consumer_dict[socket.id][0].id)
            consumer_dict[socket.id][0].close();
            delete consumer_dict[socket.id];
        }
        if(consumer_transport_dict[socket.id]){
            for (i = 0; i < consumer_transport_dict[socket.id].length; i++){
                console.log("consumer transport id : ",consumer_transport_dict[socket.id].id)
                consumer_transport_dict[socket.id][i].close();
            }
            delete consumer_transport_dict[socket.id];
        }
        // console.log(consumer_dict[socket.id])
        // console.log(consumer_transport_dict[socket.id])

        // delete producer;
        // producer_dict[socket.id].close();
        // producer_transport_dict[socket.id].close();

        console.log("PRODUCER CHECKING")
        if(producer_dict[socket.id]){
            socket.broadcast.emit("disconnectedConsumer",{producerId: producer_dict[socket.id].id})
            delete producer_dict[socket.id];
        }
        
        delete producer_transport_dict[socket.id];
    })
})


async function createConsumer(rtpCapabilities, consumer_transport_id, producer_id, socket_id){
    console.log("===================== CREATING CONSUMER =====================");
    console.log("GOT PRODUCER ID : ",producer_id)

    if (!router.canConsume(
        {
          producerId: producer_id,
          rtpCapabilities,
        })
    ) {
        console.error('can not consume');
        return;
    }

    let consumer_transport;

    console.log("CONSUMER_TRANSPORT_ID : ",consumer_transport_id);
    for(let i = 0; i < consumer_transport_dict[socket_id].length; i++){
        if(consumer_transport_dict[socket_id][i].id == consumer_transport_id){
            consumer_transport = consumer_transport_dict[socket_id][i];
            break;
        }
    }

    console.log("CONSUMER TRANSPORT FOUND(?)");
    console.log(consumer_transport != null);

    let created_consumer;
    try{
        created_consumer = await consumer_transport.consume({
            producerId: producer.id,
            rtpCapabilities,
            paused: producer.kind === 'video',
        });

        created_consumer.on("transportclose",() =>{
            console.log("Consumer Closed ID : ",created_consumer.id);
        })
    }catch(error) {
        console.error('consume failed',error);
        return;
    }

    if(created_consumer.type === 'simulcast'){
        await created_consumer.setPrefferedLayers({ spatialLayer : 2, temporalLayer: 2 })
    }
    try{
        consumer_dict[socket_id].push(created_consumer);
    }catch(error){
        consumer_dict[socket_id] = [created_consumer];
    }
    return {
        producerId: producer_id,
        id: created_consumer.id,
        kind: created_consumer.kind,
        rtpParameters: created_consumer.rtpParameters,
        type: created_consumer.type,
        producerPaused: created_consumer.producerPaused
    };
}

async function runMediasoupWorker(){
    console.log("Creating Mediasoup worker & router...");
    worker = await mediasoup.createWorker({
        logLevel: 'warn',
        logTags: [
            'info',
            'ice',
            'dtls',
            'rtp',
            'srtp',
            'rtcp'
          ],
        //  PORT 부족하면 사용.
        rtcMinPort: 2000,
        rtcMaxPort: 3000,
    });

    worker.on('died', () => {
        console.error('mediasoup worker died, exiting in 2 seconds... [pid:%d]', worker.pid);
        setTimeout(() => process.exit(1), 2000);
    });

    const mediaCodecs = [
        {
          kind: 'audio',
          mimeType: 'audio/opus',
          clockRate: 48000,
          channels: 2
        },
        {
          kind: 'video',
          mimeType: 'video/VP8',
          clockRate: 90000,
          parameters:
            {
              'x-google-start-bitrate': 1000
            }
        },
    ]
    router = await worker.createRouter({ mediaCodecs });

    console.log('Medisoup worker & router created');
    console.log("worker PID : ",worker.pid);
    console.log("router ID : ",router.id);
}

async function createWebRtcTransport() {
    try{
        const webRtcTransport_options = {
            listenIps: [
              {
                ip: '0.0.0.0',
                announcedIp: '127.0.0.1', //replace with relevant IP address
              }
            ],
            enableUdp: true,
            enableTcp: true,
            preferUdp: true,
        }
        let transport = await router.createWebRtcTransport(webRtcTransport_options);
        transport.on('dtlsstatechange', dtlsState => {
            if (dtlsState === 'closed') {
              transport.close()
            }
        })
        
        transport.on('close', () => {
            console.log('transport closed')
        })

        return {
            transport,
            params: {
                id: transport.id,
                iceParameters: transport.iceParameters,
                iceCandidates: transport.iceCandidates,
                dtlsParameters: transport.dtlsParameters,
              }
        }

    }catch(error){
        console.log(error)
    }
}