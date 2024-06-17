package org.studyspring.demo.bean.transaction;


import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserInsertListener {

	/**
	 * 标注有@EventListener注解（包括@TransactionalEventListener）的方法的访问权限最低是protected的
	 * 另外可以在监听方法上标注@Order来控制执行顺序哦
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = {UserInsertEvent.class})
	public void onUserInsertEvent(UserInsertEvent event) {
		/**
		 * TransactionalEventListenerFactory
		 */

		System.out.println("UserInsertListener 方法 onUserInsertEvent 线程是: " + Thread.currentThread().getName());
		System.out.println("event: " + event.toString());
	}

}
