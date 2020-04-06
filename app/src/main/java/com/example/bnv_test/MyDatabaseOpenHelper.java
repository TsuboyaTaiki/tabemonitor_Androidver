package com.example.bnv_test;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseOpenHelper extends SQLiteOpenHelper {

    // 定数宣言
    // ログ用文字列
    private static final String LOG_TAG = "DEBUG_" + MyDatabaseOpenHelper.class.getSimpleName();

    private static final int DB_VERSION = 1;                        // データベースのバージョン
    private static final String DB_NAME = "tabemonitorDataBase.db";       // データベースの名前

    // フードテーブルのテーブル名、カラム名の定義
    static final String TABLE_NAME_FOOD = "FoodTable";                          // テーブル名
    static final String FOOD_COLUMN_ID = "id";                                  // 食材ID
    static final String FOOD_COLUMN_FOOD_NAME = "food_name";                    // 食材名
    static final String FOOD_COLUMN_FOOD_NAME_HIRAGANA = "food_name_hiragana";  // 食材名(平仮名表記)
    static final String FOOD_COLUMN_RECOGNITION_STR = "recogn_str";             // 認識用文字列
    static final String FOOD_COLUMN_STOCK = "stock";                            // 在庫数量
    static final String FOOD_COLUMN_UNIT_NAME = "unit_name";                    // 単位名
    static final String FOOD_COLUMN_STOCK_FLUCTUATION = "fluctuation";          // 増減量
    static final String FOOD_COLUMN_EXPIRATION = "expiration";                  // 賞味期限
    static final String FOOD_COLUMN_CLASSIFICATION = "classification";          // 分類
    static final String FOOD_COLUMN_DETAILS = "details";                        // 詳細分類

    // レシピテーブルのテーブル名、カラム名の定義
    static final String TABLE_NAME_RECIPE = "RecipeTable";               // テーブル名
    static final String RECIPE_COLUMN_ID = "id";                         // レシピID
    static final String RECIPE_COLUMN_RECIPE_NAME = "recipe_name";       // レシピ名
    static final String RECIPE_COLUMN_CLASSIFICATION = "classification"; // 分類
    static final String RECIPE_COLUMN_DETAILS = "details";               // 詳細分類
    static final String RECIPE_COLUMN_FOOD_ID = "food_id";               // 使用食材ID

    // 食材テーブルのCREATE文
    private static final String SQL_CREATE_FOOD_TABLE =
            "CREATE TABLE " + TABLE_NAME_FOOD + "(" +
                    FOOD_COLUMN_ID +                    " TEXT PRIMARY KEY," +  // 食材ID
                    FOOD_COLUMN_FOOD_NAME +             " TEXT," +                // 食材名
                    FOOD_COLUMN_FOOD_NAME_HIRAGANA +    " TEXT," +                // 食材名(平仮名表記)
                    FOOD_COLUMN_RECOGNITION_STR +       " TEXT," +                // 認識用文字列
                    FOOD_COLUMN_STOCK +                 " INTEGER," +             // 在庫数量
                    FOOD_COLUMN_UNIT_NAME +             " TEXT," +                // 単位名
                    FOOD_COLUMN_STOCK_FLUCTUATION +     " INTEGER," +             // 増減量
                    FOOD_COLUMN_EXPIRATION +            " DATE," +                // 賞味期限
                    FOOD_COLUMN_CLASSIFICATION +        " TEXT," +                // 分類
                    FOOD_COLUMN_DETAILS +               " TEXT" +                 // 詳細分類
                    ")";

    // レシピテーブルのCREATE文
    private static final String SQL_CREATE_RECIPE_TABLE =
            "CREATE TABLE " + TABLE_NAME_RECIPE + "(" +
                    RECIPE_COLUMN_ID +              " TEXT PRIMARY KEY," +    // レシピID
                    RECIPE_COLUMN_RECIPE_NAME +     " TEXT," +                // レシピ名
                    RECIPE_COLUMN_CLASSIFICATION +  " TEXT," +                // レシピ分類
                    RECIPE_COLUMN_DETAILS  +        " TEXT," +                // レシピ詳細分類
                    RECIPE_COLUMN_FOOD_ID  +        " TEXT" +                 // 使用食材ID
                    ")";

    // 食材テーブルのDELETE文
    private static final String SQL_DELETE_FOOD_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME_FOOD;

    // レシピテーブルのDELETE文
    private static final String SQL_DELETE_RECIPE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME_RECIPE;

    MyDatabaseOpenHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // データベースが初めて作成されるときに呼び出される
    @Override
    public void onCreate(SQLiteDatabase db){

        Log.d(LOG_TAG, "onCreate");

        // テーブルの作成
        db.execSQL(SQL_CREATE_FOOD_TABLE);
        db.execSQL(SQL_CREATE_RECIPE_TABLE);
        initialSaveData(db);
    }

    // データベースをアップグレードするときに呼び出される
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // 古いバージョンのテーブルを消して
        // 新しいバージョンのテーブルを作成する
        db.execSQL(SQL_DELETE_FOOD_TABLE);
        db.execSQL(SQL_DELETE_RECIPE_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

    private void initialSaveData(SQLiteDatabase db){
        // 初期データを記述
        // 肉類
        foodTableSaveData(db, "001","牛肩ロース","ぎゅうかたろーす","牛肩ロース_牛肩口ース_牛肩口一ス",0,"g",50,"9/15","肉","牛肉");
        foodTableSaveData(db, "002","牛バラ","ぎゅうばら","牛ばら_牛バラ",0,"g",50,"","肉","牛肉");
        foodTableSaveData(db, "003","牛もも","ぎゅうもも","牛モモ_牛もも",0,"g",50,"","肉","牛肉");
        foodTableSaveData(db, "011","豚ロース","ぶたろーす","豚ロース_豚口ース_豚口一ス",0,"g",50,"","肉","豚肉");
        foodTableSaveData(db, "012","豚バラ","ぶたばら","豚ばら_豚バラ",0,"g",50,"","肉","豚肉");
        foodTableSaveData(db, "004","牛カルビ","ぎゅうかるび","牛かるび_牛カルビ",0,"g",50,"","肉","牛肉");
        foodTableSaveData(db, "005","牛サーロイン","ぎゅうさーろいん","牛サーロイン_牛サ一ロイン_牛サー口イン_牛サ一口イン",0,"g",50,"","肉","牛肉");
        foodTableSaveData(db, "006","牛ハラミ","ぎゅうはらみ","牛はらみ_牛ハラミ",0,"g",50,"","肉","牛肉");
        foodTableSaveData(db, "021","鶏モモ","とりもも","鶏もも_鶏モモ",0,"g",50,"","肉","鶏肉");  //1枚250g
        foodTableSaveData(db, "022","鶏ムネ","とりむね","鶏むね_鶏ムネ",0,"g",50,"","肉","鶏肉");  //1枚300g
        foodTableSaveData(db, "023","ササミ","ささみ","ささみ_ササミ",0,"g",50,"","肉","鶏肉");  //1本55g
        foodTableSaveData(db, "024","手羽先","てばさき","てばさき_手羽先",0,"g",50,"","肉","鶏肉");
        foodTableSaveData(db, "025","手羽元","てばもと","てばもと_手羽元",0,"g",50,"","肉","鶏肉");
        foodTableSaveData(db, "031","ハム","はむ","はむ_ハム",0,"枚",1,"","肉","加工肉");
        foodTableSaveData(db, "032","ソーセージ","そーせーじ","ウィンナー_ウィンナ一_ソーセージ_ソ一セージ_ソーセ一ジ_ソ一セ一ジ",0,"本",1,"","肉","加工肉");
        foodTableSaveData(db, "033","ベーコン","べーこん","べーこん_べーコン_ベ一コン_べ一コン",0,"枚",1,"","肉","加工肉");
        foodTableSaveData(db, "034","チャーシュー","ちゃーしゅー","チャーシュ_チャ一シュ",0,"g",50,"","肉","加工肉");
        foodTableSaveData(db, "035","鶏ひき肉","とりひきにく","鶏ひき",0,"g",50,"","肉","鶏肉");
        foodTableSaveData(db, "036","豚肩ロース","ぶたろーす","豚肩ロース_豚肩ロ一ス_豚肩口ース_豚肩口一ス",0,"g",50,"","肉","豚肉");
        foodTableSaveData(db, "037","豚ひき肉","ぶたひきにく","豚ひき",0,"g",50,"","肉","豚肉");
        foodTableSaveData(db, "038","豚もも","ぶたももにく","豚モモ_豚もも",0,"g",50,"","肉","豚肉");
        foodTableSaveData(db, "039","合いびき肉","あいびきにく","合い挽き肉_合いびき肉",0,"g",50,"","肉","牛肉");
        foodTableSaveData(db, "040","牛ひき肉","ぎゅうひきにく","牛ひき_牛ひき肉",0,"g",50,"","肉","牛肉");

        // 野菜・果実類
        foodTableSaveData(db, "101","キャベツ","きゃべつ","キャベツ_きゃべつ",0,"玉",1,"","野菜","野菜");
        foodTableSaveData(db, "102","レタス","れたす","レタス_れたす",0,"玉",1,"","野菜","野菜");
        foodTableSaveData(db, "103","白菜","はくさい","白菜_はくさい",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "104","玉ねぎ","たまねぎ","玉ねぎ_たまねぎ_玉葱",0,"玉",1,"","野菜","野菜");	//1玉200g
        foodTableSaveData(db, "105","もやし","もやし","もやし_モヤシ",0,"袋",1,"","野菜","野菜");
        foodTableSaveData(db, "106","ほうれん草","ほうれんそう","ほうれん草_ほうれんそう",0,"株",1,"","野菜","野菜");
        foodTableSaveData(db, "107","チンゲンサイ","ちんげんさい","ちんげんさい_チンゲン菜_チンゲンサイ",0,"株",1,"","野菜","野菜");
        foodTableSaveData(db, "108","小松菜","こまつな","こまつな_コマツナ_小松菜",0,"株",1,"","野菜","野菜");
        foodTableSaveData(db, "109","ミニトマト","みにとまと","プチトマト_ミニトマト",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "110","きゅうり","きゅうり","キュウリ_きゅうり",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "111","なす","なす","ナス_なす",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "112","ピーマン","ぴーまん","ピーマン_ピ一マン",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "113","かぼちゃ","かぼちゃ","カボチャ_かぼちゃ",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "114","ゴーヤ","ごーや","にがうり_ゴーヤ",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "115","とうもろこし","とうもろこし","トウモロコシ_とうもろこし_トウモ口コシ",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "116","にんじん","にんじん","ニンジン_にんじん_人参",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "117","大根","だいこん","だいこん_大根_ダイコン",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "118","生姜","しょうが","しょうが_生姜_ショウガ",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "119","アボカド","あぼかど","アボカド_あぼかど",0,"個",1,"","野菜","果実");
        foodTableSaveData(db, "120","りんご","りんご","リンゴ_りんご",0,"個",1,"","野菜","果実");
        foodTableSaveData(db, "121","かぶ","かぶ","カブ_かぶ_蕪",0,"株",1,"","野菜","野菜");
        foodTableSaveData(db, "122","桃","もも","もも_モモ_桃",0,"個",1,"","野菜","果実");
        foodTableSaveData(db, "123","柿","かき","かき_カキ_柿",0,"個",1,"","野菜","果実");
        foodTableSaveData(db, "124","さくらんぼ","さくらんぼ","サクランボ_さくらんぼ",0,"袋",1,"","野菜","果実");
        foodTableSaveData(db, "125","いちご","いちご","イチゴ_いちご",0,"袋",1,"","野菜","果実");
        foodTableSaveData(db, "126","バナナ","ばなな","ばなな_バナナ",0,"本",1,"","野菜","果実");
        foodTableSaveData(db, "127","すいか","すいか","スイカ_すいか_西瓜",0,"個",1,"","野菜","果実");
        foodTableSaveData(db, "128","れんこん","れんこん","レンコン_れんこん_蓮根",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "129","ねぎ","ねぎ","ネギ_ねぎ",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "130","さやえんどう","さやえんどう","サヤエンドウ_さやえんどう",0,"本",1,"","野菜","豆");	//10さやで30g
        foodTableSaveData(db, "131","にんにく","にんにく","ニンニク_にんにく",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "132","パプリカ","ぱぷりか","赤ピーマン_パプリカ_黄ピーマン",0,"個",1,"","野菜","野菜");
        // 厚揚げ、油揚げを調味料欄へ移動 133 134
        foodTableSaveData(db, "135","トマト","とまと","とまと_トマト",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "136","ブロッコリー","ぶろっこりー","ぶろっこりー_ブ口ッコリー_ブロッコリ一_ブ口ッコリ一",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "137","にら","にら","ニラ_にら",0,"束",1,"","野菜","野菜");
        foodTableSaveData(db, "138","セロリ","せろり","せろり_セロリ",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "139","たけのこ","たけのこ","タケノコ_たけのこ_筍",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "140","ごぼう","ごぼう","ごぼう_ゴボウ",0,"本",1,"","野菜","野菜");
        foodTableSaveData(db, "141","こんにゃく","こんにゃく","コンニャク_こんにゃく",0,"枚",1,"","その他","野菜");
        foodTableSaveData(db, "142","豆腐","とうふ","とうふ_豆腐",0,"丁",1,"","その他","野菜");
        foodTableSaveData(db, "143","しらたき","しらたき","しらたき_シラタキ",0,"玉",1,"","その他","野菜");
        foodTableSaveData(db, "144","みょうが","みょうが","ミョウガ_みょうが_茗荷",0,"個",1,"","野菜","野菜");
        foodTableSaveData(db, "145","さやいんげん","さやいんげん","サヤインゲン_さやいんげん",0,"g",50,"","野菜","野菜");

        // 魚介類
        foodTableSaveData(db, "201","さけ","さけ","鮭_サケ_さけ",0,"g",50,"","魚介","魚介");    //1切れ100g
        foodTableSaveData(db, "202","ぶり","ぶり","鰤_ブリ_ぶり",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "203","さば","さば","鯖_さば_サバ",0,"g",50,"","魚介","魚介");    //1尾800g、1切れ200g
        foodTableSaveData(db, "204","さんま","さんま","秋刀魚_さんま_サンマ",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "205","しらす","しらす","シラス_しらす_白子",0,"パック",1,"","魚介","魚介");
        foodTableSaveData(db, "206","えび","えび","エビ_えび_海老",0,"尾",1,"","魚介","魚介");	//1尾20g
        foodTableSaveData(db, "207","いか","いか","イカ_いか",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "208","たこ","たこ","タコ_たこ",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "209","あさり","あさり","アサリ_あさり",0,"パック",1,"","魚介","貝"); //1パック250～300g
        foodTableSaveData(db, "210","しじみ","しじみ","シジミ_しじみ",0,"パック",1,"","魚介","貝");
        foodTableSaveData(db, "211","いくら","いくら","イクラ_いくら",0,"パック",1,"","魚介","魚介");
        foodTableSaveData(db, "212","タイ","たい","鯛_タイ_たい",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "213","まぐろ","まぐろ","鮪_まぐろ_マグロ",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "214","あじ","あじ","アジ_鯵_あじ_鰺",0,"尾",1,"","魚介","魚介");
        foodTableSaveData(db, "215","いわし","いわし","イワシ_いわし_鰯_鰮",0,"尾",1,"","魚介","魚介");
        foodTableSaveData(db, "216","かつお","かつお","カツオ_かつお_鰹",0,"g",50,"","魚介","魚介");
        foodTableSaveData(db, "217","かき","かき","カキ_かき_牡蠣_牡蛎",0,"個",1,"","魚介","魚介");

        // きのこ・山菜類
        foodTableSaveData(db, "301","しいたけ","しいたけ","椎茸_しいたけ_シイタケ_椎賞",0,"本",1,"","野菜","きのこ");
        foodTableSaveData(db, "302","しめじ","しめじ","シメジ_しめじ",0,"袋",1,"","野菜","きのこ");
        foodTableSaveData(db, "303","エリンギ","えりんぎ","えりんぎ_エリンギ",0,"本",1,"","野菜","きのこ");
        foodTableSaveData(db, "304","えのきたけ","えのきたけ","えのきだけ_えのきたけ_エノキダケ_エノキタケ",0,"袋",1,"","野菜","きのこ");
        foodTableSaveData(db, "305","まいたけ","まいたけ","マイタケ_まいたけ_舞茸_舞賞",0,"袋",1,"","野菜","きのこ");
        foodTableSaveData(db, "306","まつたけ","まつたけ","松茸_まつたけ_マツタケ_松賞",0,"本",1,"","野菜","きのこ");

        // 卵類
        foodTableSaveData(db, "401","鶏卵","けいらん","たまご_卵_タマゴ_玉子",0,"個",1,"","その他","卵");

        // いも類
        foodTableSaveData(db, "501","じゃがいも","じゃがいも","ジャガイモ_じゃがいも",0,"個",1,"","野菜","芋");
        foodTableSaveData(db, "502","さつまいも","さつまいも","サツマイモ_さつまいも",0,"個",1,"","野菜","芋");
        foodTableSaveData(db, "503","里芋","さといも","サトイモ_さといも_里芋",0,"個",1,"","野菜","芋");

        // ごはん
        foodTableSaveData(db, "601","ごはん","","",0,"",0,"","def","def");
        foodTableSaveData(db, "602","リングイーネ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "603","スパゲッティ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "604","マカロニ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "605","中華蒸し麺","","",0,"",0,"","def","def");
        foodTableSaveData(db, "606","中華生麺","","",0,"",0,"","def","def");
        
        // 調味料
        foodTableSaveData(db, "701","塩","","しお",0,"",0,"","def","def");
        foodTableSaveData(db, "702","胡椒","","こしょう",0,"",0,"","def","def");
        foodTableSaveData(db, "703","醤油","","しょうゆ",0,"",0,"","def","def");
        foodTableSaveData(db, "704","サラダ油","","さらだあぶら",0,"",0,"","def","def");
        foodTableSaveData(db, "705","ケチャップ","","けちゃっぷ",0,"",0,"","def","def");
        foodTableSaveData(db, "706","マッシュルーム缶","","まっしゅるーむ",0,"",0,"","def","def");
        foodTableSaveData(db, "707","糸三つ葉","","いとみつば",0,"",0,"","def","def");
        foodTableSaveData(db, "708","だし汁","","だしじる",0,"",0,"","def","def");
        foodTableSaveData(db, "709","砂糖","","さとう",0,"",0,"","def","def");
        foodTableSaveData(db, "710","パセリ","","ぱせり",0,"",0,"","def","def");
        foodTableSaveData(db, "711","酢","","す",0,"",0,"","def","def");
        foodTableSaveData(db, "712","みりん","","みりん",0,"",0,"","def","def");
        foodTableSaveData(db, "713","カレー粉","","かれーこ",0,"",0,"","def","def");
        foodTableSaveData(db, "714","唐辛子","","とうがらし",0,"",0,"","def","def");
        foodTableSaveData(db, "715","赤ワイン","","あかわいん",0,"",0,"","def","def");
        foodTableSaveData(db, "716","トマトの水煮","","とまとのみずに",0,"",0,"","def","def");
        foodTableSaveData(db, "717","ガラムマサラ","","がらむまさら",0,"",0,"","def","def");
        foodTableSaveData(db, "718","マーマレード","","まーまれーど",0,"",0,"","def","def");
        foodTableSaveData(db, "719","ウスターソース","","うーすたーそーす",0,"",0,"","def","def");
        foodTableSaveData(db, "720","ナツメグ","","なつめぐ",0,"",0,"","def","def");
        foodTableSaveData(db, "721","レモン汁","","レモン汁",0,"",0,"","def","def");
        foodTableSaveData(db, "722","小麦粉","","こむぎこ",0,"",0,"","def","def");
        foodTableSaveData(db, "723","バター","","ばたー",0,"",0,"","def","def");
        foodTableSaveData(db, "724","トマトピューレ","","とまとぴゅーれ",0,"",0,"","def","def");
        foodTableSaveData(db, "725","固形スープの素(チキン)","","固定スープの素",0,"",0,"","def","def");
        foodTableSaveData(db, "726","ローリエ","","ろーりえ",0,"",0,"","def","def");
        foodTableSaveData(db, "727","パルメザンチーズ","","ぱるめざんちーず",0,"",0,"","def","def");
        foodTableSaveData(db, "728","生クリーム","","なまくりーむ",0,"",0,"","def","def");
        foodTableSaveData(db, "729","白ワイン","","白ワイン",0,"",0,"","def","def");
        foodTableSaveData(db, "730","粗びき黒こしょう","","粗びき黒こしょう",0,"",0,"","def","def");
        foodTableSaveData(db, "731","オリーブ油","","おりーぶゆ",0,"",0,"","def","def");
        foodTableSaveData(db, "732","牛乳","","牛乳",0,"",0,"","def","def");
        foodTableSaveData(db, "733","粉チーズ","","こなちーず",0,"",0,"","def","def");
        foodTableSaveData(db, "734","酒","","さけ",0,"",0,"","def","def");
        foodTableSaveData(db, "735","片栗粉","","かたくりこ",0,"",0,"","def","def");
        foodTableSaveData(db, "736","水","","みず",0,"",0,"","def","def");
        foodTableSaveData(db, "737","豆板醤","","とうばんじゃん",0,"",0,"","def","def");
        foodTableSaveData(db, "738","甜麺醤","","てんめんじゃん",0,"",0,"","def","def");
        foodTableSaveData(db, "739","紹興酒","","しょうこうしゅ",0,"",0,"","def","def");
        foodTableSaveData(db, "740","鶏がらスープの素","","",0,"",0,"","def","def");
        foodTableSaveData(db, "741","しょうが汁","","しょうがじる",0,"",0,"","def","def");
        foodTableSaveData(db, "742","ごま油","","ごまあぶら",0,"",0,"","def","def");
        foodTableSaveData(db, "743","ラー油","","らーゆ",0,"",0,"","def","def");
        foodTableSaveData(db, "744","粒マスタード","","つぶますたーど",0,"",0,"","def","def");
        foodTableSaveData(db, "745","オイスターソース","","オイスターソース",0,"",0,"","def","def");
        foodTableSaveData(db, "746","練りがらし","","ねりがらし",0,"",0,"","def","def");
        foodTableSaveData(db, "747","春雨","","はるさめ",0,"",0,"","def","def");
        foodTableSaveData(db, "748","揚げ油","","あげあぶら",0,"",0,"","def","def");
        foodTableSaveData(db, "749","パン粉","","ぱんこ",0,"",0,"","def","def");
        foodTableSaveData(db, "750","とんかつソース","","とそ",0,"",0,"","def","def");
        foodTableSaveData(db, "751","みそ","","味噌",0,"",0,"","def","def");
        foodTableSaveData(db, "752","ワンタンの皮","","わんたん",0,"",0,"","def","def");
        foodTableSaveData(db, "753","お湯","","",0,"",0,"","def","def");
        foodTableSaveData(db, "754","トマトジュース","","",0,"",0,"","def","def");
        foodTableSaveData(db, "755","牛脂","","",0,"",0,"","def","def");
        foodTableSaveData(db, "756","デミグラスソース","","",0,"",0,"","def","def");
        foodTableSaveData(db, "757","ホールコーン","","",0,"",0,"","def","def");
        foodTableSaveData(db, "758","かまぼこ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "759","ぎんなん","","",0,"",0,"","def","def");
        foodTableSaveData(db, "760","ゆずの輪切り","","",0,"",0,"","def","def");
        foodTableSaveData(db, "761","中濃ソース","","",0,"",0,"","def","def");
        foodTableSaveData(db, "762","わかめ","","わかめ",0,"",0,"","def","def");
        foodTableSaveData(db, "763","きくらげ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "764","ヤングコーンの缶詰","","",0,"",0,"","def","def");
        foodTableSaveData(db, "765","ブラックオリーブ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "766","いり白ごま","","",0,"",0,"","def","def");
        foodTableSaveData(db, "767","韓国のり","","",0,"",0,"","def","def");
        foodTableSaveData(db, "768","唐辛子のみじん切り","","",0,"",0,"","def","def");
        foodTableSaveData(db, "769","白菜キムチ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "770","すり白ごま","","",0,"",0,"","def","def");
        foodTableSaveData(db, "771","粉唐辛子","","",0,"",0,"","def","def");
        foodTableSaveData(db, "772","コチュジャン","","",0,"",0,"","def","def");
        foodTableSaveData(db, "773","マヨネーズ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "774","七味唐辛子","","",0,"",0,"","def","def");
        foodTableSaveData(db, "775","昆布","","こんぶ",0,"",0,"","def","def");
        foodTableSaveData(db, "776","ごぼう巻き","","",0,"",0,"","def","def");
        foodTableSaveData(db, "777","さつま揚げ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "778","揚げボール","","",0,"",0,"","def","def");
        foodTableSaveData(db, "779","つみれ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "780","ちくわ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "781","だし汁十昆布のもどし汁","","",0,"",0,"","def","def");
        foodTableSaveData(db, "782","厚揚げ","","",0,"",0,"","def","def");
        foodTableSaveData(db, "783","油揚げ","","",0,"",0,"","def","def");

        // データベース追加コピー用
        // foodTableSaveData(db, "","","",,"",,"","","");
        // foodTableSaveData(db, "","","",0,"",0,"","def","def");

        //レシピリスト// ID,レシピ名,分類,いらない,※食材ID_個数　終わりは「」　　調味料の量を書いてないのは適量
        recipeTableSaveData(db,"001","炒飯","ご飯","","401_2_034_100_112_2_129_1_601_800g_701_適量_702_少量_703_大さじ1強_704_大さじ4");//塩、こしょう:少量、醤油:大さじ1強、サラダ油:大さじ4
        recipeTableSaveData(db,"002","オムライス","ご飯","","022_300_104_1_401_8_601_500g_706_50g_701_適量_702_適量_705_大さじ8_710_適量");//マッシュルーム:50g、塩、こしょう、ケチャップ:大さじ8、パセリ
        recipeTableSaveData(db,"003","親子丼","ご飯","","022_300_401_4_601_800g_707_1束_708_1カップ_703_大さじ3強_709_大さじ1_712_大さじ2");//糸三つ葉:1束、だし汁:1カップ、しょうゆ:大さじ3強、砂糖:大さじ1、みりん:大さじ2
        recipeTableSaveData(db,"004","そぼろ丼","ご飯","","401_6_035_300_130_3_601_800g_703_適量_711_小さじ1/2_709_適量_701_適量_741_大さじ1/2_712_大さじ1_708_1/2カップ");//醤油、酢:小さじ1/2、砂糖、塩、しょうが汁:大さじ1/2、みりん:大さじ1、だし汁:1/2
        recipeTableSaveData(db,"005","チキンカレー","ご飯","","021_600_104_1_131_2_601_適量_701_適量_702_適量_713_適量_714_1～2本_715_1/4カップ_716_400g_704_大さじ2_717_少量_718_大さじ1_719_大さじ1");//塩、こしょう、カレー粉、唐辛子:1～2本、赤ワイン:1/4カップ、トマトの水煮:400g、サラダ油:大さじ2、ガラムマサラ:少量、マーマレード:大さじ1、ウースターソース:大さじ1
        recipeTableSaveData(db,"006","ハヤシライス","ご飯","","003_300_104_1_131_1_601_適量_722_大さじ6_723_大さじ4_724_1/2カップ_725_1個_726_1枚_701_適量_705_大さじ2_719_大さじ2_703_適量_715_大さじ3_720_少量_721_大さじ1_709_小さじ2");
        recipeTableSaveData(db,"007","カルボナーラ","ご飯","","033_5_401_2_602_300g_701_適量_727_100g_728_1/2カップ_729_1/3カップ_730_適量");
        recipeTableSaveData(db,"008","ペペロンチーノ","ご飯","","131_2_603_300g_714_4本_731_大さじ6_710_適量_701_適量");//唐辛子:4本、オリーブ油:大さじ6、イタリアンパセリのみじん切り、塩
        recipeTableSaveData(db,"009","トマトソーススパゲッティ","ご飯","","104_1_131_1_603_300g_716_2缶_731_大さじ2_701_適量");//トマトの水煮:2缶、オリーブ油:大さじ2、塩
        recipeTableSaveData(db,"010","マカロニグラタン","ご飯","","022_300_206_7_104_1_604_100g_706_1缶_704_大さじ1_701_適量_702_適量_723_適量_722_大さじ6_732_3カップ_733_大さじ2");//バター、小麦粉、牛乳:3カップ、粉チーズ
        recipeTableSaveData(db,"011","あんかけ焼きそば","ご飯","","001_160_108_1_132_2_129_1_206_12_118_1_605_4玉_703_適量_734_適量_704_適量_701_適量_702_適量_740_適量_735_大さじ4_736_大さじ6");//片栗粉:大さじ4、水:大さじ6
        recipeTableSaveData(db,"012","タンメン","ご飯","","036_100_101_1_116_1_105_1_130_16_606_4玉_740_大さじ1/3_701_適量_702_適量_734_適量_704_適量");//こしょう、酒、サラダ油
        recipeTableSaveData(db,"013","麻婆豆腐","肉","","037_140_118_1_129_1_740_小さじ1_737_大さじ1/2_738_大さじ1/3_735_大さじ2弱_736_大さじ4_703_適量_739_適量_702_適量_704_大さじ3");//こしょう、サラダ油:大さじ3
        recipeTableSaveData(db,"014","小松菜と厚揚げの煮びたし","野菜類","","108_1_782_1枚_708_1/3カップ_703_大さじ1_734_大さじ1_712_大さじ1/2_701_小さじ1/5");//塩:小さじ1/5
        recipeTableSaveData(db,"015","ひじきの煮物","野菜類","","022_100_116_1_783_1枚_734_大さじ1_708_1/2カップ_709_大さじ1/2_703_大さじ1_704_小さじ1/2");
        recipeTableSaveData(db,"016","豚肉の生姜焼き","肉","豚肉","011_300_101_1_741_小さじ1_703_適量_734_大さじ1/2_704_大さじ1_712_大さじ1/2");
        recipeTableSaveData(db,"017","ポークソテー","肉","豚肉","011_400_116_1_130_23_701_適量_702_適量_722_適量_729_大さじ2_704_大さじ1_725_1/4個_715_大さじ2_719_大さじ1_705_大さじ1_720_少量_703_小さじ1/2");
        recipeTableSaveData(db,"018","焼き餃子","肉","豚肉","037_100_103_200_129_1_137_1_701_適量_703_小さじ2_741_小さじ1_739_大さじ1_702_少量_742_大さじ1/2強_704_適量_711_適量_743_適量");
        recipeTableSaveData(db,"019","チンジャオロース","肉","豚肉","038_200_112_6_132_2_401_1_701_適量_702_適量_703_適量_734_小さじ1_704_小さじ1_735_適量_734_小さじ1_709_小さじ1_736_大さじ3");
        recipeTableSaveData(db,"020","回鍋肉","肉","豚肉","012_120_101_1_129_1_118_2_131_2_734_適量_703_適量_701_少量_704_適量_738_大さじ3_737_小さじ2_703_小さじ2_709_小さじ2_702_少量");
        recipeTableSaveData(db,"021","ポトフ","肉","豚肉","036_600_116_2_104_2_101_1_138_2_726_2本_725_2個_701_適量_702_少量_744_適量");
        recipeTableSaveData(db,"022","シューマイ","肉","豚肉","037_240_104_1_301_2_139_1_102_1_703_適量_709_大さじ1/2_739_大さじ1_745_小さじ1/3_738_小さじ1_711_適量_746_適量");
        recipeTableSaveData(db,"023","春巻き","肉","豚肉","036_160_139_1_301_2_137_1_105_1_747_30g_745_大さじ1_703_適量_709_大さじ1_701_小さじ1/3_742_少量_702_少量_735_大さじ1/2_736_適量_722_大さじ1/2_704_大さじ1_748_適量_711_適量_746_適量");
        recipeTableSaveData(db,"024","とんカツ","肉","豚肉","011_150_401_1_101_1_701_少量_702_少量_722_適量_749_適量_748_適量_750_適量");
        recipeTableSaveData(db,"025","豚汁","肉","豚肉","038_150_503_3_116_1_140_1_141_1_129_1_301_6_701_適量_711_少量_704_大さじ1/2_751_大さじ4～5");
        recipeTableSaveData(db,"026","ワンタン","肉","豚肉","037_150_129_1_118_1_107_2_752_1袋_701_小さじ1/2弱_702_適量_703_適量_734_小さじ1/2～1_742_小さじ1/2～1_740_小さじ2");
        recipeTableSaveData(db,"027","ハンバーグ","肉","牛肉","039_300_401_1_104_1_136_1_109_12_701_適量_702_少量_720_少量_749_1/3カップ_732_大さじ2_704_適量_734_大さじ2_715_大さじ3_705_大さじ3_719_大さじ3");
        recipeTableSaveData(db,"028","肉じゃが","肉","牛肉","001_200_501_4_104_1_130_5_704_小さじ2_708_3カップ_709_大さじ3_712_大さじ2_734_大さじ2_703_大さじ2_701_少量");
        recipeTableSaveData(db,"029","ロールキャベツ","肉","牛肉","039_200_101_1_104_1_401_1_749_1/3カップ_701_適量_702_適量_720_少量_753_1カップ_725_1個_726_1枚_709_小さじ1_754_1/2カップ");
        recipeTableSaveData(db,"030","ハヤシライス","肉","牛肉","003_300_104_1_131_1_701_適量_702_少量_706_50g_722_大さじ6_723_大さじ4_724_1/2カップ_725_1個_726_1枚_705_大さじ2_719_大さじ2_703_大さじ1_715_大さじ3_720_少量_721_大さじ1_709_小さじ2");
        recipeTableSaveData(db,"031","すき焼き","肉","牛肉","001_500_142_1_143_1_129_3_301_8_401_4_703_1/2カップ_709_大さじ3_712_1/2カップ_708_1/2カップ_755_適量");
        recipeTableSaveData(db,"032","ビーフシチュー","肉","牛肉","003_400_131_1_104_1_116_1_501_4_701_適量_702_適量_723_大さじ2_715_1/4カップ_756_2缶_725_1個_726_1枚_753_3カップ");
        recipeTableSaveData(db,"033","チキンソテー","肉","鶏肉","021_500_112_5_701_適量_702_適量_734_大さじ2_757_1缶_704_大さじ1/2");
        recipeTableSaveData(db,"034","筑前煮","肉","鶏肉","301_6_116_1_140_1_128_1_022_200_141_1_130_7_704_大さじ1強_708_2カップ_709_大さじ3_712_大さじ3弱_703_大さじ5");
        recipeTableSaveData(db,"035","クリームシチュー","肉","鶏肉","021_2_104_1_116_1_501_3_136_1_131_1_701_適量_702_適量_723_大さじ4_722_大さじ6_725_1個_726_1枚_732_1カップ");
        recipeTableSaveData(db,"036","フライドチキン","肉","鶏肉","025_500_131_1_701_小さじ1/2_702_少量_722_適量_748_適量_710_適量");
        recipeTableSaveData(db,"037","厚焼き卵","肉","卵","401_4_701_小さじ1/3_703_適量_709_大さじ2_734_大さじ2_704_適量");
        recipeTableSaveData(db,"038","オムレツ","肉","卵","401_8_701_適量_702_適量_732_大さじ8_723_大さじ4");
        recipeTableSaveData(db,"039","茶碗蒸し","肉","卵","401_3_023_55_206_4_301_4_758_4切れ_759_8粒_707_8本_708_3カップ_701_適量_703_適量_734_適量");
        recipeTableSaveData(db,"040","かき玉汁","肉","卵","401_2_708_適量_701_小さじ1/2強_703_小さじ1_741_少量_735_大さじ1/2");
        recipeTableSaveData(db,"041","ぶりの照り焼き","魚介類","魚","202_400_703_大さじ3_712_大さじ2_760_2～3枚_704_大さじ1/2");
        recipeTableSaveData(db,"042","鮭のムニエル","魚介類","魚","201_400_701_適量_702_適量_722_適量_723_大さじ1_729_大さじ1_704_大さじ1");
        recipeTableSaveData(db,"043","あじの塩焼き","魚介類","魚","214_4_701_適量");
        recipeTableSaveData(db,"044","さばのみそ煮","魚介類","魚","203_800_140_1_118_2_751_大さじ5_709_大さじ4弱_734_大さじ3_712_大さじ2_736_2カップ_711_少量");
        recipeTableSaveData(db,"045","いわしのしょうが煮","魚介類","魚","215_8_118_1_734_1/2カップ_712_大さじ2_703_大さじ4_736_2カップ");
        recipeTableSaveData(db,"046","あじの南蛮漬け","魚介類","魚","214_12_129_1_722_適量_748_適量_714_1本_703_1/3カップ_711_1/3カップ_709_小さじ1_741_1片分");
        recipeTableSaveData(db,"047","かつおのたたき","魚介類","魚","216_350_104_1_129_3_144_3_118_1_704_少量_701_適量_721_大さじ3_703_大さじ3");
        recipeTableSaveData(db,"048","かきフライ","魚介類","貝類","217_20_401_1_701_適量_702_少量_722_適量_749_適量_748_適量_761_適量");
        recipeTableSaveData(db,"049","あさりのみそ汁","魚介類","貝類","209_1_701_適量_751_大さじ2～3_707_8～12本");
        recipeTableSaveData(db,"050","たことわかめときゅうりの酢のもの","魚介類","えび・いか・たこ","208_200_110_2_762_20g_701_適量_711_大さじ3/2_709_小さじ1_703_小さじ1/4_708_大さじ1");
        recipeTableSaveData(db,"051","八宝菜","魚介類","えび・いか・たこ","206_100_207_80_012_120_139_1_116_1_103_1_130_2_763_4g_764_6本_734_適量_735_適量_741_少量_703_適量_701_適量_740_小さじ1/2弱_704_大さじ1_736_大さじ1");
        recipeTableSaveData(db,"052","えびフライ","魚介類","えび・いか・たこ","206_12_401_1_701_少量_702_少量_722_適量_749_適量_748_適量");
        recipeTableSaveData(db,"053","魚介のマリネ","魚介類","えび・いか・たこ","208_150_209_1_206_8_104_1_138_1_132_1_131_1_729_大さじ7_701_適量_702_適量_711_大さじ3_731_大さじ2_765_12個");
        recipeTableSaveData(db,"054","ナムル","野菜類","葉野菜","106_1_116_1_105_1_131_1_701_適量_703_小さじ1_742_適量_766_適量_709_適量_767_適量");
        recipeTableSaveData(db,"055","チヂミ","野菜類","葉野菜","401_2_116_1_131_1_129_1_722_100g_736_1カップ_768_小さじ1_701_少量_742_大さじ4");
        recipeTableSaveData(db,"056","チゲ鍋","野菜類","葉野菜","142_1_129_1_305_1_131_1_769_100g_725_3/2個_736_4カップ_770_大さじ1_703_小さじ1_742_小さじ1_702_少量_771_小さじ1/4～1/2_772_適量");
        recipeTableSaveData(db,"057","いんげんのごま和え","野菜類","果菜","145_200_701_適量_708_大さじ3_703_3/2_770_大さじ6_709_大さじ2");
        recipeTableSaveData(db,"058","ゴーヤーチャンプルー","野菜類","果菜","114_1_142_1_036_100_401_2_701_適量_702_適量_703_小さじ2_704_大さじ1弱_742_大さじ1弱");
        recipeTableSaveData(db,"059","かぼちゃの煮物","野菜類","果菜","113_1_708_2カップ_709_大さじ3弱_703_大さじ1/2_701_小さじ1/2");
        recipeTableSaveData(db,"060","きんぴら","野菜類","根菜","140_1_116_1_711_少量_708_1/2カップ_709_大さじ3/2_703_大さじ2_734_大さじ2_704_小さじ2_742_小さじ2_714_1/2本");
        recipeTableSaveData(db,"061","里芋の煮っころがし","野菜類","根菜","503_16_701_大さじ1/3～1/2_708_2～3カップ_709_大さじ3_712_大さじ2_702_大さじ2_734_大さじ2");
        recipeTableSaveData(db,"062","ポテトサラダ","野菜類","根菜","501_5_110_1_104_1_031_2_701_適量_702_適量_711_大さじ1_773_大さじ6");
        recipeTableSaveData(db,"063","ポテトコロッケ","野菜類","根菜","501_5_039_100_104_1_401_1_109_12_102_1_701_小さじ1/3_702_少量_704_大さじ1/2_722_適量_749_適量_748_適量");
        recipeTableSaveData(db,"064","けんちん汁","野菜類","根菜","142_1_117_1_116_1_301_4_141_1_704_大さじ1/2_708_5カップ_703_大さじ5/2_734_大さじ2_701_小さじ1/4_774_適量");
        recipeTableSaveData(db,"065","おでん","野菜類","根菜","117_1_141_1_401_4_775_2枚_776_4本_777_小4枚_778_4串_779_4個_780_大1本_781_8カップ_703_大さじ3_734_大さじ3_709_大さじ3_701_大さじ1_746_適量");
        recipeTableSaveData(db,"066","まつたけご飯(6人分)","ご飯","きのこ","306_4_601_3合_708_3カップ_703_大さじ1_701_小さじ3/4_734_大さじ2");
        recipeTableSaveData(db,"067","たけのこご飯(6人分)","ご飯","たけのこ","139_1_783_1枚_775_1枚_601_3合_703_大さじ3/2_701_小さじ2/3_712_大さじ1");

        // データベース追加コピー用
        //recipeTableSaveData(db,"","","","","");
    }

    // 食材テーブルのデータ保存メソッド
     private void foodTableSaveData(
            SQLiteDatabase db,
            String id,              // ID
            String foodName,        // 食材名
            String foodNameHiragana,// 食材名(平仮名表記)
            String recogn_str,      // 認識用文字列
            int stock,              // 在庫数量
            String unit_name,       // 単位名
            int fluctuation,        // 増減量
            String date,            // 賞味期限
            String classification,  // 分類
            String details          // 詳細分類
    ){
        ContentValues values = new ContentValues();
        values.put(FOOD_COLUMN_ID, id);
        values.put(FOOD_COLUMN_FOOD_NAME, foodName);
        values.put(FOOD_COLUMN_FOOD_NAME_HIRAGANA, foodNameHiragana);
        values.put(FOOD_COLUMN_RECOGNITION_STR, recogn_str);
        values.put(FOOD_COLUMN_STOCK, stock);
        values.put(FOOD_COLUMN_UNIT_NAME, unit_name);
        values.put(FOOD_COLUMN_STOCK_FLUCTUATION, fluctuation);
        values.put(FOOD_COLUMN_EXPIRATION, date);
        values.put(FOOD_COLUMN_CLASSIFICATION, classification);
        values.put(FOOD_COLUMN_DETAILS, details);

        db.insert(TABLE_NAME_FOOD, null, values);
    }

    // レシピテーブルのデータ保存メソッド
    private void recipeTableSaveData(
            SQLiteDatabase db,
            String id,              // ID
            String recipeName,      // レシピ名
            String classification,  // 分類
            String details,         // 詳細分類
            String foodId           // 使用食材ID
    ){
        // レシピ名のログ
        Log.d(LOG_TAG, recipeName);

        // 食材IDを昇順に並び替える
        foodId = recipeFoodIdSort(foodId);

        // データベースに挿入
        ContentValues values = new ContentValues();
        values.put(RECIPE_COLUMN_ID, id);
        values.put(RECIPE_COLUMN_RECIPE_NAME, recipeName);
        values.put(RECIPE_COLUMN_CLASSIFICATION, classification);
        values.put(RECIPE_COLUMN_DETAILS, details);
        values.put(RECIPE_COLUMN_FOOD_ID,foodId);

        db.insert(TABLE_NAME_RECIPE, null, values);
    }

    // レシピの食材IDを昇順に並び替えるメソッド
    String recipeFoodIdSort(String recipeFoodId){
        String[] tmp = recipeFoodId.split("_");
        String[] foodId = new String[tmp.length / 2];
        String[] foodConsume = new String[tmp.length / 2];

        // レシピの食材IDを食材IDと消費数に分割する
        for(int i = 0, j = 0; i < tmp.length / 2; i++, j += 2){
            foodId[i] = tmp[j];
            foodConsume[i] = tmp[j + 1];
        }
        
        // 食材IDを昇順に並び替える
        for(int i = 0; i < foodId.length; i++){
            for(int j = i + 1; j < foodId.length; j++){
                if(Integer.valueOf(foodId[i]) > Integer.valueOf(foodId[j])){
                    tmp[0] = foodId[i];
                    foodId[i] = foodId[j];
                    foodId[j] = tmp[0];
                    
                    tmp[0] = foodConsume[i];
                    foodConsume[i] = foodConsume[j];
                    foodConsume[j] = tmp[0];
                }
            }
        }
        
        // 食材ID、消費数を再びレシピ食材IDとして連結させる
        StringBuilder recipeFoodIdBuilder = new StringBuilder();
        for(int i = 0; i < foodId.length; i++){
            recipeFoodIdBuilder.append(foodId[i]);
            recipeFoodIdBuilder.append("_");
            recipeFoodIdBuilder.append(foodConsume[i]);
            if(i != foodId.length -1){
                recipeFoodIdBuilder.append("_");
            }
        }

        // レシピの食材ID変更のログ
        Log.d(LOG_TAG, recipeFoodId);
        Log.d(LOG_TAG, recipeFoodIdBuilder.toString());

        // 並び替えた食材IDを返す
        return recipeFoodIdBuilder.toString();
    }

    // データベースに登録されている食材の総数を返すメソッド
    int getFoodsAmount(SQLiteDatabase db){

        int amount = db.query(
                TABLE_NAME_FOOD,
                new String[]{FOOD_COLUMN_ID},
                null, null, null,
                null, null
        ).getCount();

        return amount;
    }

    // データベースに登録されているレシピの総数を返すメソッド
    int getRecipesAmount(SQLiteDatabase db){

        int amount = db.query(
                TABLE_NAME_RECIPE,
                new String[]{FOOD_COLUMN_ID},
                null, null, null,
                null, null
        ).getCount();

        return amount;
    }
}
