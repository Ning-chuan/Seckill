import cn.hutool.core.util.IdUtil;
import com.yuziyan.seckill.dao.RedisDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class MyTest {




    /**
     * 用于测试：null与布尔值的转换
     */
    @Test
    public void test(){
        //编译报错：
//        if (null){
//            System.out.println("null测试");
//        }
        long time = new Date().getTime();
        System.out.println("time = " + time);
    }


}
