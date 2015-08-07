package org.prosolo.bigdata.scala
import org.scalatest._
import collection.mutable.Stack
/**
 * @author zoran Aug 6, 2015
 */
class ListTest extends FlatSpec with Matchers {
 "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.push(3)
    stack.pop() should be (3)
    //stack.pop() should be (2)
    assert(stack.pop()===2)
    assertResult(1){
      stack.pop()
    }
  //  assert(stack.pop()===1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    } 
  }
}