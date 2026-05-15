package lui

import com.raquo.laminar.api.L.{Mod as _, *}

/** Input prop: feeds a `Source[V]` into the component. */
final class In[V, El <: Component](bind: (El, Source[V]) => Unit) {
  inline def :=(v: V): Mod[El] = el => bind(el, Signal.fromValue(v))
  inline def <--(s: Source[V]): Mod[El] = el => bind(el, s)
}

/** Output prop: routes a `Sink[V]` consumer of the component's events. */
final class Out[V, El <: Component](bind: (El, Sink[V]) => Unit) {
  inline def -->(s: Sink[V]): Mod[El] = el => bind(el, s)

  /** Every event becomes the constant `value`. Lets you write
    * `Button.click.mapTo(View.Workbench) --> view.writer`. */
  def mapTo[B](value: B): Out[B, El] = new Out[B, El]((el, sinkB) =>
    bind(el, sinkB.toObserver.contramap[V](_ => value))
  )

  /** Map every outgoing event through `f`. */
  def map[B](f: V => B): Out[B, El] = new Out[B, El]((el, sinkB) =>
    bind(el, sinkB.toObserver.contramap[V](f))
  )

  /** Run an arbitrary side effect on each event. Use for fire-and-forget cases like
    * `Button.click.foreach(_ => Theme.toggle())`. */
  def foreach(f: V => Unit): Mod[El] = el => bind(el, Observer[V](f))
}

/** Two-way prop: both directions plus a `<-->` binding to a `Var[V]`. */
final class InOut[V, El <: Component](
    bindIn: (El, Source[V]) => Unit,
    bindOut: (El, Sink[V]) => Unit
) {
  inline def :=(v: V): Mod[El] = el => bindIn(el, Signal.fromValue(v))
  inline def <--(s: Source[V]): Mod[El] = el => bindIn(el, s)
  inline def -->(s: Sink[V]): Mod[El] = el => bindOut(el, s)
  inline def <-->(v: Var[V]): Mod[El] = el => {
    bindIn(el, v.signal)
    bindOut(el, v.writer)
  }
}

/** Boilerplate helpers for the common case where a prop directly feeds one of the
  * component's internal Vars / EventBuses. Use these inside the component companion:
  *
  * {{{
  *   val label   = Prop.in[String, Button](_.labelVar)
  *   val variant = Prop.in[Variant, Button](_.variantVar)
  *   val click   = Prop.out[Unit, Button](_.clickBus)
  *   val open    = Prop.inOut[Boolean, Modal](_.openVar)
  * }}}
  */
object Prop {

  def in[V, El <: Component](getVar: El => Var[V]): In[V, El] =
    new In[V, El]((el, src) => {
      val _ = el.root.amend(src.toObservable --> getVar(el).writer)
    })

  def out[V, El <: Component](getBus: El => EventBus[V]): Out[V, El] =
    new Out[V, El]((el, sink) => {
      val _ = el.root.amend(getBus(el).events --> sink)
    })

  /** Round-trips through a single Var: external `Source` writes into it; its `signal.changes`
    * stream emits to external `Sink`s. */
  def inOut[V, El <: Component](getVar: El => Var[V]): InOut[V, El] =
    new InOut[V, El](
      bindIn = (el, src) => {
        val _ = el.root.amend(src.toObservable --> getVar(el).writer)
      },
      bindOut = (el, sink) => {
        val _ = el.root.amend(getVar(el).signal.changes.distinct --> sink)
      }
    )
}
