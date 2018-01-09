# GodotFireBase
GodotFireBase é um módulo que permite a integração dos serviços do FireBase nos projetos desenvolvidos com a Godot.

Adaptado de `https://github.com/FrogSquare/GodotFireBase`.

## Depende De

> Godot game engine (3.0): `git clone https://github.com/godotengine/godot`

> GodotSQL: `git clone https://github.com/FrogSquare/GodotSQL`

## Recursos Disponíveis
> AdMob

> Analytics

> Authentication [W.I.P] Google, Facebook, Twitter, Email

> Firebase Notification

> RemoteConfig

> Storage

> Invites (Email & SMS)

> Firestore (W.I.P)

## Configurações Antes da Compilação

Copie seu arquivo `google-services.json` para `[GODOT-ROOT]/platform/android/java/` e edite o arquivo `modules/FireBase/config.py`, substituindo `com.your.appid` pelo package do seu projeto.

```
env.android_add_default_config("applicationId 'com.your.appid'")
```

Caso vá utilizar o RemoteConfigs, configure os parâmetros padrões no arquivo `.xml` que se encontra em `[GODOT-ROOT]/modules/FireBase/res/xml/remote_config_defaults.xml`.

## Inicializando o Módulo
Edite o arquivo `engine.cfg`, adicionando o seguinte código:

```
[android]
modules="org/godotengine/godot/FireBase,org/godotengine/godot/SQLBridge"
```

## Referenciando e Inicializando

### Na Godot 2.X

```
var firebase = Globals.get_singleton("FireBase");
```

### Na Godot 3.X

```
var firebase = Engine.get_singleton("FireBase");
```

## Configurando

### Crie o arquivo `godot-firebase-config.json` com o conteúdo abaixo na pasta do seu projeto.

```
{
	"AdMob" : true,
	"Authentication" : true,
	"Invites" : true,
	"RemoteConfig" : true,
	"Notification" : true,
	"Storage" : true,
	"FireStore" : true,

	"AuthConf" : 
	{
		"Google" : true,
		"Twitter" : true,
		"Facebook" : true,
		"FacebookAppId" : "1234566789875"
	},

	"Ads" : 
	{
		"AppId": "YOUR_APP_ID_HERE",
		"BannerAd" : true,
		"BannerGravity" : "BOTTOM",
		"BannerAdId" : "",

		"InterstitialAd" : true,
		"InterstitialAdId" : "",

		"RewardedVideoAd" : true,
		"RewardedVideoAdId" : ""
	}
}

```

Inicialize o módulo com o arquivo de configurações:

```
func _ready():
    if OS.get_name() == "Android":
        firebase.initWithFile("res://godot-firebase-config.json", get_instance_ID());
```

## Recebendo mensagens do Java:
Para receber dados do Java:
```
func _receive_message(tag, from, key, data):
    if tag == "FireBase":
        if from == "<from>":
            if key == "<key>":
                print(data)
```

## FireBase Analytics
```
firebase.send_events("EventName", Dictionary);
firebase.send_custom("TestKey", "SomeValue");

firebase.setScreenName("Screen_name")
firebase.sendAchievement("someAchievementId")		# unlock achievement
firebase.join_group("clan_name")			# join clan/group
firebase.level_up("character_name", level)		# send character level
firebase.post_score("charcter name", level, score)	# post your score
firebase.earn_currency("currency", amount);		# when play earn some virtual currency gold/Diamond/any
firebase.spend_currency("item_id", "currency", amount)	# when user spend virtual currency

firebase.tutorial_begin()				# tutorial begin
firebase.tutorial_complete()				# tutorial end

Reference: https://support.google.com/firebase/answer/6317494?hl=en
```

## Disparando um AlertDialog

```
firebase.alert("Message goes here..!");
```

## Firebase Firestore

### Escrever
Adicionar dados a um documento: (os dados são substituídos)
```
firebase.set_document("nome_da_coleção", "nome_do_documento", dicionario)
```
Adicionar dados sem especificar o documento:
```
firebase.add_document("collection_name", dicionario)
```
Ambos retornam um bool indicando sucesso ou não para:
```
from = "Firestore", key = "DocumentAdded"
```

### Ler
Carregar os documentos de uma coleção:
```
firebase.load_document("collection_name")
```
Retorna os documentos em JSON para:
```
from = "Firestore", key = "Documents"
```

### Listener
Setar listener em um documento:
```
firebase.set_listener("nome_da_coleção", "nome_do_documento")
```
Remover listener de um documento:
```
firebase.remove_listener("nome_da_coleção", "nome_do_documento")
```
Quando há uma modificação dos dados observados pelo listener, os mesmos são enviados em JSON para:
```
from = "Firestore", key = "SnapshotData"
```

## Autenticação

### Facebook

Edite o arquivo `res/values/ids.xml` substituindo o `facebook_app_id` pelo id do seu app do Facebook.

Sign In:
```
firebase.facebook_sign_in()
```
Sign Out:
```
firebase.facebook_sign_out()
```
Obter dados do usuário (nome, email e foto(uri)):
```
firebase.get_facebook_user()
```
Revogar acesso:
```
firebase.facebook_revoke_access();
```
Verificar se está conectado:
```
firebase.is_facebook_connected()
```
Permissões:
```
firebase.facebook_has_permission("publish_actions") // Checar se está permitido

firebase.revoke_facebook_permission("publish_actions") // Revogar permissão

firebase.ask_facebook_publish_permission("publish_actions"); // Pedir permissão de publish

firebase.ask_facebook_read_permission("email"); // Pedir permissão de leitura

firbase.get_facebook_permissions() // Listar permissões
```

### Google
Sign In:
```
firebase.google_sign_in()
```
Sigh Out:
```
firebase.google_sign_out()
```
Obter dados do usuário (nome, email e foto(uri)):
```
var gUserDetails = firebase.get_google_user()
```
Revogar acesso:
```
firebase.google_revoke_access();
```
Verificar se está conectado:
```
firebase.is_google_connected()
```

### Email e Senha
Sign In:
```
firebase.email_sign_in("email", "senha")
```
Sign Out:
```
firebase.email_sign_out()
```
Criar conta:
```
firebase.email_create_account("email", "senha")
```
Obter dados do usuário (email e user_id):
```
firebase.get_email_user()
```
Verificar se está conectado:
```
firebase.is_email_connected()
```

### Anônimo:
Sign In:
```
firebase.anonymous_sign_in()
```
Sign Out:
```
firebase.anonymous_sign_out()
```
Verificar se está conectado:
```
firebase.is_anonymous_connected()
```

## Firebase Notification

```
firebase.subscribeToTopic("topic") // Subscribe to particular topic.
firebase.getToken() // Get current client TokenID

If recived notifiction has a payload, it will be saved inside SQL Database under key: "firebase_notification_data"

firebase.notifyInMins("message", 60) // Shedule notification in 60 min
```

## RemoteConfig API
```
firebase.getRemoteValue("remote_key") // Return String value
```
## Configurando os Valores Padrões do RemoteConfig
```
var defs = Dictionary()
defs["some_remoteconfig_key1"] = "remote_config_value1"
defs["some_remoteconfig_key2"] = "remote_config_value2"

firebase.setRemoteDefaults(defs.to_json())
```
Ou carregue por um arquivo JSON:
```
firebase.setRemoteDefaultsFile("res://path/to/jsonfile.json")
```

## Firebase Storage

```
Upload Files from sdcard
firebase.upload("images/file", "destFolder") // uploads file from sdcard to firebase

Download Files from Firebase
firebase.download("file", "images"); // Saves file from firebase to sdcard
```

## Firebase Invites
```
Invite Friends with Email & SMS, DeepLink example: https://play.google.com/store/apps/details?id=[package-id].

firebase.invite("message", "https://example.com/beed/link") // Send Firebase Invites.
firebase.invite("message", "");  // Fallback to use default android share eg: Whatsapp, Twitter and more.
```

## Firebase AdMob
```
firebase.show_banner_ad(true)	// Show Banner Ad
firebase.show_banner_ad(false)	// Hide Banner Ad
firebase.set_banner_unitid("unit_id") // Change current Ad unit ID

firebase.show_interstitial_ad() // Show Interstitial Ad
firebase.show_rewarded_video()	// Show Rewarded Video Ad
firebase.show_rvideo("unit_id") // Show Rewarded Video Ad

firebase.request_rewarded_video_status() // Request the rewarded video status
```

AdMob Recive message from java
```
func _receive_message(tag, from, key, data):
    if tag == "FireBase" and from == "AdMob":
        if key == "AdMobReward":
            # when rewared video play complete
            print("json data with [RewardType & RewardAmount]: ", data);

        elif key == "AdMob_Video":
            # when rewarded video loaded
            # data will be `loaded` or `load_failed and `loaded` or `not_loaded` with `firebase.request_rewarded_video_status()`
            print("AdMob rewarded video status is ", data);

        elif key == "AdMob_Banner":
            # when banner loaded
            # data will be `loaded` or `load_failed`
            print("Banner Status: ", data);

        elif key == "AdMob_Interstitial" and data == "loaded":
            # when Interstitial loaded
            # data will be `loaded` or `load_failed`
            print("Interstitial Status: ", data);
```

## Atenção!

Quando for exportar não se esqueça de incluir os arquivos `*.json`:
![alt text](http://preview.ibb.co/fTwC8Q/Screenshot_from_2017_06_17_18_44_25.png)

## Eventos de Log
Utilize o comando abaixo para observar os logs emitidos pelo dispositivo:
```
adb -d logcat godot:V FireBase:V DEBUG:V AndroidRuntime:V ValidateServiceOp:V *:S
```
