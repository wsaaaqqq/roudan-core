package test.orm;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.xht.xdb.orm.EntityServiceImp;
import test.orm.po.PoJPA;

public class SpelTest {
    private static final EntityServiceImp<PoJPA> dao = new EntityServiceImp<>(PoJPA.class, "h2");

    public static void main(String[] args) {
        // 模拟Spring容器中的Bean
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("zdDao", dao);
        context.refresh(); // 刷新上下文以激活Bean工厂
        // 创建StandardEvaluationContext并配置BeanResolver
        StandardEvaluationContext standardContext = new StandardEvaluationContext();
        standardContext.setBeanResolver(new BeanFactoryResolver(context)); // 关键！
        // 调用Bean方法（成功）
        ExpressionParser parser = new SpelExpressionParser();
        String expression = "@zdDao.getById('98B0B5604FCA95EAE05317D0E80AE02B')";
        Expression ex = parser.parseExpression(expression);
        PoJPA result = ex.getValue(standardContext, PoJPA.class);
        System.out.println("结果：" + result); // 输出：用户_123
    }
}
