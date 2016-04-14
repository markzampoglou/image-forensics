package gr.iti.mklab.reveal.web;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kandreadou on 1/9/15.
 */
public class Requests {

    /**
     * {"collection":"today","urls":["http://static4.businessinsider.com/image/5326130f69bedd780c549606-1200-924/putin-68.jpg","http://www.trbimg.com/img-531a4ce6/turbine/topic-peplt007593"]
     * }
     */
    public class IndexPostRequest {

        public String collection;

        public Set<String> urls = new HashSet<>();
    }

    /**
     * {
     "text":"After%20the%20shock%20and%20the%20horror%20came%20the%20time%20for%20collective%20revival.%20No%20one%20in%20France%20can%20recall%20anything%20like%20this%20since%20the%20images%20of%20Paris%20at%20the%20time%20of%20the%20liberation%20in%201944.%20Then%2C%20as%20now%2C%20we%20as%20a%20nation%20were%20desperate%20to%20reaffirm%20what%20our%20republic%20was%20meant%20to%20be%2C%20how%20it%20wanted%20to%20survive%2C%20how%20it%20would%20overcome%20barbarous%20aggression%2C%20as%20well%20as%20the%20country%E2%80%99s%20failures%20and%20divisions.%0A%0AUp%20to%202%20million%20people%20poured%20on%20to%20the%20streets%20of%20Paris%20%E2%80%93%20and%20an%20estimated%203.7%20million%20across%20France%20%E2%80%93%20in%20a%20demonstration%20of%20unity%20against%20terror%20and%20in%20defence%20of%20values%20that%20are%20at%20the%20heart%20of%20democracy%2C%20and%20at%20the%20heart%20of%20Europe.%20Faces%20were%20determined%20and%20emotional.%0A%0AFamilies%20had%20brought%20their%20children%2C%20including%20babies%20in%20prams%2C%20so%20that%20every%20generation%20may%20take%20part%20in%20this%20moment%20of%20history%20in%20the%20making.%20In%20the%20compact%2C%20solemn%20crowd%2C%20people%20felt%20the%20need%20to%20speak%20about%20their%20different%20ethnic%2C%20religious%20and%20social%20backgrounds%20while%20holding%20signs%20that%20said%20%E2%80%9Cfraternity%2C%20freedom%2C%20republic%E2%80%9D%2C%20intent%20on%20proving%20that%20gunmen%20could%20never%20be%20victorious%20in%20dividing%20a%20nation%2C%20nor%20in%20weakening%20the%20very%20essence%20of%20European%20humanis"
     }
     */
    public class EntitiesPostRequest {

        public String text;
    }
}
